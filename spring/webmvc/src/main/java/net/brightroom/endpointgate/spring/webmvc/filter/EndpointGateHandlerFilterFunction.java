package net.brightroom.endpointgate.spring.webmvc.filter;

import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.core.provider.ConditionProvider;
import net.brightroom.endpointgate.core.provider.RolloutPercentageProvider;
import net.brightroom.endpointgate.spring.webmvc.condition.HttpServletConditionVariables;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webmvc.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * A factory for {@link HandlerFilterFunction} that applies endpoint gate access control to
 * Functional Endpoints.
 *
 * <p>Use {@link #of(String)} to create a {@link HandlerFilterFunction} for a specific gate ID and
 * apply it to a {@link org.springframework.web.servlet.function.RouterFunction}:
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

  private final EndpointGateEvaluationPipeline pipeline;
  private final AccessDeniedHandlerFilterResolution resolution;
  private final RolloutPercentageProvider rolloutPercentageProvider;
  private final ConditionProvider conditionProvider;
  private final EndpointGateContextResolver contextResolver;

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified endpoint gate.
   *
   * <p>Condition and rollout percentage are resolved from the configured providers.
   *
   * @param gateId the identifier of the endpoint gate to check; must not be null or blank
   * @return a {@link HandlerFilterFunction} that allows or denies access based on the endpoint gate
   * @throws IllegalArgumentException if {@code gateId} is null or blank
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(String gateId) {
    return of(gateId, "", 100);
  }

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified endpoint gate
   * and fallback condition expression.
   *
   * <p>The condition is resolved from the provider first; the {@code conditionFallback} is used
   * only when the provider returns no value for the gate.
   *
   * @param gateId the identifier of the endpoint gate to check; must not be null or blank
   * @param conditionFallback SpEL expression used as fallback when the provider has no condition
   *     configured; empty string means no condition
   * @return a {@link HandlerFilterFunction} that allows or denies access based on the endpoint gate
   *     and condition
   * @throws IllegalArgumentException if {@code gateId} is null or blank
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(
      String gateId, String conditionFallback) {
    return of(gateId, conditionFallback, 100);
  }

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified endpoint gate
   * and fallback rollout percentage.
   *
   * <p>The rollout percentage is resolved from the provider first; the {@code rolloutFallback} is
   * used only when the provider returns no value for the gate.
   *
   * @param gateId the identifier of the endpoint gate to check; must not be null or blank
   * @param rolloutFallback the fallback rollout percentage (0–100) when no value is configured in
   *     the provider; 100 means fully enabled
   * @return a {@link HandlerFilterFunction} that allows or denies access based on the endpoint gate
   *     and rollout
   * @throws IllegalArgumentException if {@code gateId} is null or blank, or if {@code
   *     rolloutFallback} is not between 0 and 100
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(
      String gateId, int rolloutFallback) {
    return of(gateId, "", rolloutFallback);
  }

  /**
   * Creates a {@link HandlerFilterFunction} that guards the route with the specified endpoint gate,
   * fallback SpEL condition expression, and fallback rollout percentage.
   *
   * <p>The condition and rollout percentage are resolved from their respective providers first;
   * fallback values are used only when the providers return no value for the gate.
   *
   * <p>The evaluation order is: gate enabled check → schedule check → condition check → rollout
   * check.
   *
   * @param gateId the identifier of the endpoint gate to check; must not be null or blank
   * @param conditionFallback SpEL expression used as fallback when the provider has no condition
   *     configured; empty string means no condition
   * @param rolloutFallback the fallback rollout percentage (0–100) when no value is configured in
   *     the provider; 100 means fully enabled
   * @return a {@link HandlerFilterFunction} that allows or denies access based on the endpoint
   *     gate, condition, and rollout
   * @throws IllegalArgumentException if {@code gateId} is null or blank, or if {@code
   *     rolloutFallback} is not between 0 and 100
   */
  public HandlerFilterFunction<ServerResponse, ServerResponse> of(
      String gateId, String conditionFallback, int rolloutFallback) {
    if (gateId == null || gateId.isBlank()) {
      throw new IllegalArgumentException(
          "gateId must not be null or blank. "
              + "A blank value causes fail-open behavior and allows access unconditionally.");
    }
    if (rolloutFallback < 0 || rolloutFallback > 100) {
      throw new IllegalArgumentException(
          "rollout must be between 0 and 100, but was: " + rolloutFallback);
    }
    return (request, next) -> {
      String condition = conditionProvider.getCondition(gateId).orElse(conditionFallback);
      int rollout = rolloutPercentageProvider.getRolloutPercentage(gateId).orElse(rolloutFallback);
      EvaluationContext context =
          new EvaluationContext(
              gateId,
              condition,
              rollout,
              HttpServletConditionVariables.build(request.servletRequest()),
              () -> contextResolver.resolve(request.servletRequest()).orElse(null));
      AccessDecision decision = pipeline.evaluate(context);
      if (decision instanceof AccessDecision.Denied denied) {
        return resolution.resolve(request, denied.toException());
      }
      return next.handle(request);
    };
  }

  /**
   * Creates a new {@link EndpointGateHandlerFilterFunction}.
   *
   * @param pipeline the evaluation pipeline that performs all endpoint gate checks; must not be
   *     null
   * @param resolution the resolution strategy invoked when access is denied; must not be null
   * @param rolloutPercentageProvider the provider used to look up the rollout percentage per gate;
   *     must not be null
   * @param conditionProvider the provider used to look up the condition expression per gate; must
   *     not be null
   * @param contextResolver the resolver used to obtain the endpoint gate context from the request;
   *     must not be null
   */
  public EndpointGateHandlerFilterFunction(
      EndpointGateEvaluationPipeline pipeline,
      AccessDeniedHandlerFilterResolution resolution,
      RolloutPercentageProvider rolloutPercentageProvider,
      ConditionProvider conditionProvider,
      EndpointGateContextResolver contextResolver) {
    this.pipeline = pipeline;
    this.resolution = resolution;
    this.rolloutPercentageProvider = rolloutPercentageProvider;
    this.conditionProvider = conditionProvider;
    this.contextResolver = contextResolver;
  }
}
