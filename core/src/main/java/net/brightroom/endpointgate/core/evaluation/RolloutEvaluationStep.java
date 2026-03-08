package net.brightroom.endpointgate.core.evaluation;

import java.util.Optional;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.rollout.RolloutStrategy;

/** Evaluation step that checks whether the request is within the rollout bucket. */
public class RolloutEvaluationStep implements EvaluationStep {

  private final RolloutStrategy rolloutStrategy;

  /**
   * Creates a new {@code RolloutEvaluationStep}.
   *
   * @param rolloutStrategy the strategy used to determine rollout bucket membership
   */
  public RolloutEvaluationStep(RolloutStrategy rolloutStrategy) {
    this.rolloutStrategy = rolloutStrategy;
  }

  @Override
  public Optional<AccessDecision> evaluate(EvaluationContext context) {
    if (context.rolloutPercentage() >= 100) {
      return Optional.empty();
    }
    EndpointGateContext gateContext = context.gateContextSupplier().get();
    if (gateContext == null) {
      return Optional.empty(); // fail-open: no context available
    }
    if (!rolloutStrategy.isInRollout(context.gateId(), gateContext, context.rolloutPercentage())) {
      return Optional.of(AccessDecision.denied(context.gateId(), DeniedReason.ROLLOUT_EXCLUDED));
    }
    return Optional.empty();
  }
}
