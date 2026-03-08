package net.brightroom.endpointgate.reactive.core.evaluation;

import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.condition.ReactiveEndpointGateConditionEvaluator;
import reactor.core.publisher.Mono;

/** Reactive evaluation step that checks the condition expression. */
public class ReactiveConditionEvaluationStep implements ReactiveEvaluationStep {

  private final ReactiveEndpointGateConditionEvaluator conditionEvaluator;

  /**
   * Creates a new {@code ReactiveConditionEvaluationStep}.
   *
   * @param conditionEvaluator the reactive evaluator used to evaluate condition expressions
   */
  public ReactiveConditionEvaluationStep(
      ReactiveEndpointGateConditionEvaluator conditionEvaluator) {
    this.conditionEvaluator = conditionEvaluator;
  }

  @Override
  public Mono<AccessDecision> evaluate(EvaluationContext context) {
    if (context.condition().isEmpty()) {
      return Mono.just(AccessDecision.allowed());
    }
    return conditionEvaluator
        .evaluate(context.condition(), context.variables())
        .map(
            passed ->
                passed
                    ? AccessDecision.allowed()
                    : AccessDecision.denied(context.gateId(), DeniedReason.CONDITION_NOT_MET));
  }
}
