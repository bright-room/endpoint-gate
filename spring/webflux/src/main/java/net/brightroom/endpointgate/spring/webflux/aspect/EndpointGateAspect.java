package net.brightroom.endpointgate.spring.webflux.aspect;

import java.lang.reflect.Method;
import net.brightroom.endpointgate.core.annotation.EndpointGate;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.spring.webflux.condition.ServerHttpConditionVariables;
import net.brightroom.endpointgate.spring.webflux.context.ReactiveEndpointGateContextResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * AOP aspect that enforces endpoint gate access control on Spring WebFlux controller methods.
 *
 * <p>Intercepts methods and classes annotated with {@link
 * net.brightroom.endpointgate.core.annotation.EndpointGate} via an {@code @Around} advice. If the
 * referenced gate is disabled, a {@link
 * net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException} is emitted into the
 * reactive pipeline. The method-level annotation takes priority over the class-level annotation.
 *
 * <p>Only reactive return types ({@link reactor.core.publisher.Mono} and {@link
 * reactor.core.publisher.Flux}) are supported; non-reactive return types throw {@link
 * IllegalStateException}.
 */
@Aspect
public class EndpointGateAspect {

  private final ReactiveEndpointGateEvaluationPipeline pipeline;
  private final ReactiveEndpointGateContextResolver contextResolver;
  private final ReactiveRolloutPercentageProvider rolloutPercentageProvider;
  private final ReactiveConditionProvider conditionProvider;

  /**
   * Around advice that checks the endpoint gate before proceeding with the annotated method.
   *
   * <p>Applies to methods and classes annotated with {@link
   * net.brightroom.endpointgate.core.annotation.EndpointGate}. If the gate is disabled, a {@link
   * net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException} is returned in
   * the reactive pipeline. Rollout percentage and condition are also evaluated when configured.
   *
   * @param joinPoint the proceeding join point of the intercepted method
   * @return the result of the intercepted method, or an error signal if access is denied
   * @throws Throwable if the underlying method throws an exception
   */
  @Around(
      "@within(net.brightroom.endpointgate.core.annotation.EndpointGate) || "
          + "@annotation(net.brightroom.endpointgate.core.annotation.EndpointGate)")
  public Object checkEndpointGate(ProceedingJoinPoint joinPoint) throws Throwable {
    EndpointGate annotation = resolveAnnotation(joinPoint);
    if (annotation == null) {
      return joinPoint.proceed();
    }

    String[] gateIds = annotation.value();
    validateGateIds(gateIds);

    Class<?> returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();

    Mono<AccessDecision> decisionMono = buildDecisionMono(gateIds);

    if (Mono.class.isAssignableFrom(returnType)) {
      return decisionMono.flatMap(
          decision -> {
            if (decision instanceof AccessDecision.Denied denied) {
              return Mono.error(denied.toException());
            }
            return proceedAsMono(joinPoint);
          });
    }

    if (Flux.class.isAssignableFrom(returnType)) {
      return decisionMono.flatMapMany(
          decision -> {
            if (decision instanceof AccessDecision.Denied denied) {
              return Flux.error(denied.toException());
            }
            return proceedAsFlux(joinPoint);
          });
    }

    throw new IllegalStateException(
        "@EndpointGate on WebFlux controller method '"
            + ((MethodSignature) joinPoint.getSignature()).getMethod().getName()
            + "' requires a reactive return type (Mono or Flux). "
            + "Non-reactive return types are not supported.");
  }

  private Mono<AccessDecision> buildDecisionMono(String[] gateIds) {
    return Mono.deferContextual(
        ctx -> {
          ServerWebExchange exchange = ctx.get(ServerWebExchange.class);
          return Flux.fromArray(gateIds)
              .concatMap(gateId -> evaluateSingleGate(exchange, gateId))
              .filter(decision -> decision instanceof AccessDecision.Denied)
              .next()
              .defaultIfEmpty(AccessDecision.allowed());
        });
  }

  private Mono<AccessDecision> evaluateSingleGate(ServerWebExchange exchange, String gateId) {
    return Mono.zip(
            conditionProvider.getCondition(gateId).defaultIfEmpty(""),
            rolloutPercentageProvider.getRolloutPercentage(gateId).defaultIfEmpty(100),
            contextResolver
                .resolve(exchange.getRequest())
                .map(java.util.Optional::of)
                .defaultIfEmpty(java.util.Optional.empty()))
        .flatMap(
            tuple -> {
              EvaluationContext evalCtx =
                  new EvaluationContext(
                      gateId,
                      tuple.getT1(),
                      tuple.getT2(),
                      ServerHttpConditionVariables.build(exchange.getRequest()),
                      () -> tuple.getT3().orElse(null));
              return pipeline.evaluate(evalCtx);
            });
  }

  @SuppressWarnings("unchecked")
  private Mono<Object> proceedAsMono(ProceedingJoinPoint joinPoint) {
    try {
      return (Mono<Object>) joinPoint.proceed();
    } catch (Throwable t) {
      return Mono.error(t);
    }
  }

  @SuppressWarnings("unchecked")
  private Flux<Object> proceedAsFlux(ProceedingJoinPoint joinPoint) {
    try {
      return (Flux<Object>) joinPoint.proceed();
    } catch (Throwable t) {
      return Flux.error(t);
    }
  }

  private EndpointGate resolveAnnotation(ProceedingJoinPoint joinPoint) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Method method =
        AopUtils.getMostSpecificMethod(
            methodSignature.getMethod(), joinPoint.getTarget().getClass());
    EndpointGate methodAnnotation = AnnotationUtils.findAnnotation(method, EndpointGate.class);
    if (methodAnnotation != null) {
      return methodAnnotation;
    }
    return AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), EndpointGate.class);
  }

  private void validateGateIds(String[] gateIds) {
    if (gateIds.length == 0) {
      throw new IllegalStateException(
          "@EndpointGate must specify at least one non-empty value. "
              + "An empty value causes fail-open behavior and allows access unconditionally.");
    }
    for (String gateId : gateIds) {
      if (gateId.isEmpty()) {
        throw new IllegalStateException(
            "@EndpointGate must specify a non-empty value. "
                + "An empty value causes fail-open behavior and allows access unconditionally.");
      }
    }
  }

  /**
   * Creates a new {@code EndpointGateAspect}.
   *
   * @param pipeline the reactive evaluation pipeline that performs all endpoint gate checks; must
   *     not be null
   * @param contextResolver the resolver used to extract context from the current request; must not
   *     be null
   * @param rolloutPercentageProvider the provider used to look up the rollout percentage per gate;
   *     must not be null
   * @param conditionProvider the provider used to look up the condition expression per gate; must
   *     not be null
   */
  public EndpointGateAspect(
      ReactiveEndpointGateEvaluationPipeline pipeline,
      ReactiveEndpointGateContextResolver contextResolver,
      ReactiveRolloutPercentageProvider rolloutPercentageProvider,
      ReactiveConditionProvider conditionProvider) {
    this.pipeline = pipeline;
    this.contextResolver = contextResolver;
    this.rolloutPercentageProvider = rolloutPercentageProvider;
    this.conditionProvider = conditionProvider;
  }
}
