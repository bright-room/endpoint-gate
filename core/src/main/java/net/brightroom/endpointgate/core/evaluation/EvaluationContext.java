package net.brightroom.endpointgate.core.evaluation;

import java.util.function.Supplier;
import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Immutable context object passed through the {@link EndpointGateEvaluationPipeline}.
 *
 * <p>Contains all inputs required by the default evaluation steps. Callers are responsible for
 * resolving the rollout percentage (merging provider value and annotation fallback) before building
 * this context.
 *
 * @param gateId the identifier of the gate being evaluated
 * @param condition the condition expression; empty string means no condition check. {@code null} is
 *     normalized to empty string.
 * @param rolloutPercentage the resolved rollout percentage (0–100)
 * @param variables the request context variables used for condition evaluation
 * @param gateContextSupplier lazy supplier for the context used for rollout bucketing; the supplier
 *     may return {@code null} which means fail-open (skip rollout check)
 */
public record EvaluationContext(
    @NonNull String gateId,
    @NonNull String condition,
    int rolloutPercentage,
    ConditionVariables variables,
    Supplier<@Nullable EndpointGateContext> gateContextSupplier) {

  /** Compact constructor that normalizes {@code null} condition to empty string. */
  public EvaluationContext {
    if (condition == null) {
      condition = "";
    }
  }
}
