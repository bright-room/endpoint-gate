package net.brightroom.endpointgate.spring.webmvc.filter;

import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.core.provider.ConditionProvider;
import net.brightroom.endpointgate.core.provider.RolloutPercentageProvider;
import net.brightroom.endpointgate.core.validation.GateIdValidator;
import net.brightroom.endpointgate.spring.webmvc.condition.HttpServletConditionVariables;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webmvc.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * A factory for {@link HandlerFilterFunction} that applies endpoint gate access control to
 * Functional Endpoints.
 *
 * <p>Use {@link #of(String...)} to create a {@link HandlerFilterFunction} for one or more gate IDs
 * and apply it to a {@link org.springframework.web.servlet.function.RouterFunction}:
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

  private final EndpointGateEvaluationPipeline pipeline;
  private final AccessDeniedHandlerFilterResolution resolution;
  private final RolloutPercentageProvider rolloutPercentageProvider;
  private final ConditionProvider conditionProvider;
  private final EndpointGateContextResolver contextResolver;

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
    return (request, next) -> {
      for (String gateId : gateIds) {
        String condition = conditionProvider.getCondition(gateId).orElse("");
        int rollout = rolloutPercentageProvider.getRolloutPercentage(gateId).orElse(100);
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
