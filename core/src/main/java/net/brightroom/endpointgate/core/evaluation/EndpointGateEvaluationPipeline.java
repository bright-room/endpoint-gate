package net.brightroom.endpointgate.core.evaluation;

import java.util.List;
import java.util.Optional;

/**
 * Synchronous endpoint gate evaluation pipeline.
 *
 * <p>Executes the four built-in {@link EvaluationStep}s in a fixed order:
 *
 * <ol>
 *   <li>{@link EnabledEvaluationStep} — checks whether the gate is enabled
 *   <li>{@link ScheduleEvaluationStep} — checks whether the schedule is active
 *   <li>{@link ConditionEvaluationStep} — evaluates the condition expression
 *   <li>{@link RolloutEvaluationStep} — checks rollout bucket membership
 * </ol>
 *
 * <p>Returns the first {@link AccessDecision.Denied} encountered, or {@link AccessDecision.Allowed}
 * if all steps pass.
 */
public class EndpointGateEvaluationPipeline {

  private final List<EvaluationStep> steps;

  /**
   * Creates a new {@code EndpointGateEvaluationPipeline} with the fixed evaluation order.
   *
   * @param enabledStep the step that checks whether the gate is enabled
   * @param scheduleStep the step that checks the schedule
   * @param conditionStep the step that evaluates the condition expression
   * @param rolloutStep the step that checks rollout bucket membership
   */
  public EndpointGateEvaluationPipeline(
      EnabledEvaluationStep enabledStep,
      ScheduleEvaluationStep scheduleStep,
      ConditionEvaluationStep conditionStep,
      RolloutEvaluationStep rolloutStep) {
    this.steps = List.of(enabledStep, scheduleStep, conditionStep, rolloutStep);
  }

  /**
   * Evaluates all steps in order and returns the first denied decision or {@link
   * AccessDecision#allowed()} if all steps pass.
   *
   * @param context the evaluation context
   * @return the access decision
   */
  public AccessDecision evaluate(EvaluationContext context) {
    for (EvaluationStep step : steps) {
      Optional<AccessDecision> result = step.evaluate(context);
      if (result.isPresent()) {
        return result.get();
      }
    }
    return AccessDecision.allowed();
  }
}
