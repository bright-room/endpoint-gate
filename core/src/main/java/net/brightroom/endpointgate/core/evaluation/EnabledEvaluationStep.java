package net.brightroom.endpointgate.core.evaluation;

import java.util.Optional;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.provider.EndpointGateProvider;

/** Evaluation step that checks whether the gate is enabled. */
public class EnabledEvaluationStep implements EvaluationStep {

  private final EndpointGateProvider provider;

  /**
   * Creates a new {@code EnabledEvaluationStep}.
   *
   * @param provider the provider used to check whether a gate is enabled
   */
  public EnabledEvaluationStep(EndpointGateProvider provider) {
    this.provider = provider;
  }

  @Override
  public Optional<AccessDecision> evaluate(EvaluationContext context) {
    if (!provider.isGateEnabled(context.gateId())) {
      return Optional.of(AccessDecision.denied(context.gateId(), DeniedReason.DISABLED));
    }
    return Optional.empty();
  }
}
