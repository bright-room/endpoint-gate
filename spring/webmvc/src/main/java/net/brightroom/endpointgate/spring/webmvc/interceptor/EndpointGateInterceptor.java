package net.brightroom.endpointgate.spring.webmvc.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.brightroom.endpointgate.core.annotation.EndpointGate;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.core.provider.ConditionProvider;
import net.brightroom.endpointgate.core.provider.RolloutPercentageProvider;
import net.brightroom.endpointgate.spring.webmvc.condition.HttpServletConditionVariables;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that enforces endpoint gate access control on annotated controllers.
 *
 * <p>Checks the {@link net.brightroom.endpointgate.core.annotation.EndpointGate} annotation on the
 * handler method first, then on the handler class. Method-level annotations take priority over
 * class-level annotations. If the gate is disabled, an {@link EndpointGateAccessDeniedException} is
 * thrown and handled by {@link
 * net.brightroom.endpointgate.spring.webmvc.exception.EndpointGateExceptionHandler}.
 */
public class EndpointGateInterceptor implements HandlerInterceptor {

  private final EndpointGateEvaluationPipeline pipeline;
  private final RolloutPercentageProvider rolloutPercentageProvider;
  private final ConditionProvider conditionProvider;
  private final EndpointGateContextResolver contextResolver;

  /**
   * Creates a new {@link EndpointGateInterceptor}.
   *
   * @param pipeline the evaluation pipeline that performs all endpoint gate checks; must not be
   *     null
   * @param rolloutPercentageProvider the provider that supplies per-gate rollout percentages; must
   *     not be null
   * @param conditionProvider the provider that supplies per-gate condition expressions; must not be
   *     null
   * @param contextResolver the resolver used to obtain the endpoint gate context from the request;
   *     must not be null
   */
  public EndpointGateInterceptor(
      EndpointGateEvaluationPipeline pipeline,
      RolloutPercentageProvider rolloutPercentageProvider,
      ConditionProvider conditionProvider,
      EndpointGateContextResolver contextResolver) {
    this.pipeline = pipeline;
    this.rolloutPercentageProvider = rolloutPercentageProvider;
    this.conditionProvider = conditionProvider;
    this.contextResolver = contextResolver;
  }

  @Override
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    EndpointGate annotation = resolveAnnotation(handlerMethod);
    if (annotation == null) {
      return true;
    }

    validateAnnotation(annotation);

    EvaluationContext context = buildContext(request, annotation);
    AccessDecision decision = pipeline.evaluate(context);

    if (decision instanceof AccessDecision.Denied denied) {
      throw denied.toException();
    }
    return true;
  }

  private EndpointGate resolveAnnotation(HandlerMethod handlerMethod) {
    EndpointGate methodAnnotation = handlerMethod.getMethodAnnotation(EndpointGate.class);
    if (methodAnnotation != null) {
      return methodAnnotation;
    }
    return handlerMethod.getBeanType().getAnnotation(EndpointGate.class);
  }

  private void validateAnnotation(EndpointGate annotation) {
    if (annotation.value().isEmpty()) {
      throw new IllegalStateException(
          "@EndpointGate must specify a non-empty value. "
              + "An empty value causes fail-open behavior and allows access unconditionally.");
    }
  }

  private EvaluationContext buildContext(HttpServletRequest request, EndpointGate annotation) {
    String gateId = annotation.value();
    String condition = conditionProvider.getCondition(gateId).orElse("");
    int rollout = rolloutPercentageProvider.getRolloutPercentage(gateId).orElse(100);
    return new EvaluationContext(
        gateId,
        condition,
        rollout,
        HttpServletConditionVariables.build(request),
        () -> contextResolver.resolve(request).orElse(null));
  }
}
