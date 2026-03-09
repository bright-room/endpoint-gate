package net.brightroom.endpointgate.spring.webflux.filter;

import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.spring.webflux.condition.ServerHttpConditionVariables;
import net.brightroom.endpointgate.spring.webflux.context.ReactiveEndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * A factory for {@link HandlerFilterFunction} that applies endpoint gate access control to
 * Functional Endpoints.
 *
 * <p>Use {@link #of(String)} to create a {@link HandlerFilterFunction} for a specific gate ID and
 * apply it to a {@link org.springframework.web.reactive.function.server.RouterFunction}:
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
 * <p>When the gate is disabled, the filter delegates to {@link AccessDeniedHandlerFilterResolution}
 * to build the denied response without invoking the handler. The default response format follows
 * {@code endpoint-gate.response.type} configuration, and can be customized by providing a custom
 * {@link AccessDeniedHandlerFilterResolution} bean.
 *
 * <p>Use {@link #of(String, int)} to enable gradual rollout for functional endpoints with a
 * fallback rollout percentage.
 */
public class EndpointGateHandlerFilterFunction {

  private final ReactiveEndpointGateEvaluationPipeline pipeline;
  private final AccessDeniedHandlerFilterResolution resolution;
  private final ReactiveRolloutPercentageProvider rolloutPercentageProvider;
  private final ReactiveConditionProvider conditionProvider;
  private final ReactiveEndpointGateContextResolver contextResolver;

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified gate.
   *
   * <p>Condition and rollout percentage are resolved from the configured providers.
   *
   * @param gateId the identifier of the gate to check; must not be null or blank
   * @return a {@link HandlerFilterFunction} that allows or denies access based on the gate
   * @throws IllegalArgumentException if {@code gateId} is null or blank
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(String gateId) {
    return of(gateId, "", 100);
  }

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified gate and
   * fallback condition expression.
   *
   * <p>The condition is resolved from the provider first; the {@code conditionFallback} is used
   * only when the provider returns no value for the gate.
   *
   * @param gateId the identifier of the gate to check; must not be null or blank
   * @param conditionFallback SpEL expression used as fallback when the provider has no condition
   *     configured; empty string means no condition
   * @return a {@link HandlerFilterFunction} that allows or denies access based on the gate and
   *     condition
   * @throws IllegalArgumentException if {@code gateId} is null or blank
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(
      String gateId, String conditionFallback) {
    return of(gateId, conditionFallback, 100);
  }

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified gate and
   * fallback rollout percentage.
   *
   * <p>The rollout percentage is resolved from the provider first; the {@code rolloutFallback} is
   * used only when the provider returns no value for the gate.
   *
   * @param gateId the identifier of the gate to check; must not be null or blank
   * @param rolloutFallback the fallback rollout percentage (0–100) when no value is configured in
   *     the provider; 100 means fully enabled
   * @return a {@link HandlerFilterFunction} that allows or denies access based on the gate and
   *     rollout
   * @throws IllegalArgumentException if {@code gateId} is null or blank, or if {@code
   *     rolloutFallback} is not between 0 and 100
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(
      String gateId, int rolloutFallback) {
    return of(gateId, "", rolloutFallback);
  }

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified gate, fallback
   * SpEL condition expression, and fallback rollout percentage.
   *
   * <p>The condition and rollout percentage are resolved from their respective providers first;
   * fallback values are used only when the providers return no value for the gate.
   *
   * <p>The evaluation order is: gate enabled check → schedule check → condition check → rollout
   * check.
   *
   * @param gateId the identifier of the gate to check; must not be null or blank
   * @param conditionFallback SpEL expression used as fallback when the provider has no condition
   *     configured; empty string means no condition
   * @param rolloutFallback the fallback rollout percentage (0–100) when no value is configured in
   *     the provider; 100 means fully enabled
   * @return a {@link HandlerFilterFunction} that allows or denies access based on the gate,
   *     condition, and rollout
   * @throws IllegalArgumentException if {@code gateId} is null or blank, or if {@code
   *     rolloutFallback} is not between 0 and 100
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(
      String gateId, String conditionFallback, int rolloutFallback) {
    if (gateId == null) {
      throw new IllegalArgumentException(
          "gateId must not be null or blank. "
              + "A blank value causes fail-open behavior and allows access unconditionally.");
    }
    if (gateId.isBlank()) {
      throw new IllegalArgumentException(
          "gateId must not be null or blank. "
              + "A blank value causes fail-open behavior and allows access unconditionally.");
    }
    if (rolloutFallback < 0) {
      throw new IllegalArgumentException(
          String.format("rollout must be between 0 and 100, but was: %d", rolloutFallback));
    }
    if (rolloutFallback > 100) {
      throw new IllegalArgumentException(
          String.format("rollout must be between 0 and 100, but was: %d", rolloutFallback));
    }
    return (request, next) ->
        Mono.zip(
                conditionProvider.getCondition(gateId).defaultIfEmpty(conditionFallback),
                rolloutPercentageProvider
                    .getRolloutPercentage(gateId)
                    .defaultIfEmpty(rolloutFallback),
                contextResolver
                    .resolve(request.exchange().getRequest())
                    .map(java.util.Optional::of)
                    .defaultIfEmpty(java.util.Optional.empty()))
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
                })
            .flatMap(
                decision -> {
                  if (decision instanceof AccessDecision.Denied denied) {
                    return resolution.resolve(request, denied.toException());
                  }
                  return next.handle(request);
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
