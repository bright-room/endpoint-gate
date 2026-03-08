package net.brightroom.endpointgate.core.evaluation;

import java.util.Optional;
import net.brightroom.endpointgate.core.condition.EndpointGateConditionEvaluator;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;

/** Evaluation step that checks the condition expression. */
public class ConditionEvaluationStep implements EvaluationStep {

  private final EndpointGateConditionEvaluator conditionEvaluator;

  /**
   * Creates a new {@code ConditionEvaluationStep}.
   *
   * @param conditionEvaluator the evaluator used to evaluate condition expressions
   */
  public ConditionEvaluationStep(EndpointGateConditionEvaluator conditionEvaluator) {
    this.conditionEvaluator = conditionEvaluator;
  }

  @Override
  public Optional<AccessDecision> evaluate(EvaluationContext context) {
    if (context.condition().isEmpty()) {
      return Optional.empty();
    }
    if (!conditionEvaluator.evaluate(context.condition(), context.variables())) {
      return Optional.of(AccessDecision.denied(context.gateId(), DeniedReason.CONDITION_NOT_MET));
    }
    return Optional.empty();
  }
}
