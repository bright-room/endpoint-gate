package net.brightroom.endpointgate.spring.webflux.filter;

import java.util.Optional;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.core.validation.GateIdValidator;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.spring.webflux.condition.ServerHttpConditionVariables;
import net.brightroom.endpointgate.spring.webflux.context.ReactiveEndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A factory for {@link HandlerFilterFunction} that applies endpoint gate access control to
 * Functional Endpoints.
 *
 * <p>Use {@link #of(String...)} to create a {@link HandlerFilterFunction} for one or more gate IDs
 * and apply it to a {@link org.springframework.web.reactive.function.server.RouterFunction}:
 *
 * <pre>{@code
 * @Bean
 * RouterFunction<ServerResponse> routes(EndpointGateHandlerFilterFunction endpointGateFilter) {
 *     return route()
 *         .GET("/api/gate", handler::handle)
 *         .filter(endpointGateFilter.of("my-gate"))
 *         .build();
 * }
 * }</pre>
 *
 * <p>Multiple gates can be specified for AND semantics:
 *
 * <pre>{@code
 * .filter(endpointGateFilter.of("gate-a", "gate-b"))
 * }</pre>
 *
 * <p>When the gate is disabled, the filter delegates to {@link AccessDeniedHandlerFilterResolution}
 * to build the denied response without invoking the handler. The default response format follows
 * {@code endpoint-gate.response.type} configuration, and can be customized by providing a custom
 * {@link AccessDeniedHandlerFilterResolution} bean.
 */
public class EndpointGateHandlerFilterFunction {

  private final ReactiveEndpointGateEvaluationPipeline pipeline;
  private final AccessDeniedHandlerFilterResolution resolution;
  private final ReactiveRolloutPercentageProvider rolloutPercentageProvider;
  private final ReactiveConditionProvider conditionProvider;
  private final ReactiveEndpointGateContextResolver contextResolver;

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified endpoint gates
   * using AND semantics. All specified gates must permit access; if any gate denies access, the
   * request is rejected.
   *
   * <p>Condition and rollout percentage for each gate are resolved from the configured providers.
   *
   * @param gateIds the identifiers of the endpoint gates to check; must not be null or empty, and
   *     each element must not be null or blank
   * @return a {@link HandlerFilterFunction} that allows or denies access based on all gates
   * @throws IllegalArgumentException if {@code gateIds} is null, empty, or contains null/blank
   *     elements
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(String... gateIds) {
    GateIdValidator.validateGateIds(gateIds);
    return (request, next) ->
        Flux.fromArray(gateIds)
            .concatMap(gateId -> evaluateSingleGate(request, gateId))
            .filter(decision -> decision instanceof AccessDecision.Denied)
            .next()
            .defaultIfEmpty(AccessDecision.allowed())
            .flatMap(
                decision -> {
                  if (decision instanceof AccessDecision.Denied denied) {
                    return resolution.resolve(request, denied.toException());
                  }
                  return next.handle(request);
                });
  }

  private Mono<AccessDecision> evaluateSingleGate(ServerRequest request, String gateId) {
    return Mono.zip(
            conditionProvider.getCondition(gateId).defaultIfEmpty(""),
            rolloutPercentageProvider.getRolloutPercentage(gateId).defaultIfEmpty(100),
            contextResolver
                .resolve(request.exchange().getRequest())
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty()))
        .flatMap(
            tuple -> {
              EvaluationContext evalCtx =
                  new EvaluationContext(
                      gateId,
                      tuple.getT1(),
                      tuple.getT2(),
                      ServerHttpConditionVariables.build(request.exchange().getRequest()),
                      () -> tuple.getT3().orElse(null));
              return pipeline.evaluate(evalCtx);
            });
  }

  /**
   * Creates a new {@code EndpointGateHandlerFilterFunction}.
   *
   * @param pipeline the reactive evaluation pipeline that performs all endpoint gate checks; must
   *     not be null
   * @param resolution the resolution used to build the denied response for functional endpoints;
   *     must not be null
   * @param rolloutPercentageProvider the provider used to look up the rollout percentage per gate;
   *     must not be null
   * @param conditionProvider the provider used to look up the condition expression per gate; must
   *     not be null
   * @param contextResolver the resolver used to extract context from the current request; must not
   *     be null
   */
  public EndpointGateHandlerFilterFunction(
      ReactiveEndpointGateEvaluationPipeline pipeline,
      AccessDeniedHandlerFilterResolution resolution,
      ReactiveRolloutPercentageProvider rolloutPercentageProvider,
      ReactiveConditionProvider conditionProvider,
      ReactiveEndpointGateContextResolver contextResolver) {
    this.pipeline = pipeline;
    this.resolution = resolution;
    this.rolloutPercentageProvider = rolloutPercentageProvider;
    this.conditionProvider = conditionProvider;
    this.contextResolver = contextResolver;
  }
}
