package net.brightroom.endpointgate.reactive.core.evaluation;

import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive endpoint gate evaluation pipeline.
 *
 * <p>Executes the four built-in {@link ReactiveEvaluationStep}s in a fixed order:
 *
 * <ol>
 *   <li>{@link ReactiveEnabledEvaluationStep} — checks whether the gate is enabled
 *   <li>{@link ReactiveScheduleEvaluationStep} — checks whether the schedule is active
 *   <li>{@link ReactiveConditionEvaluationStep} — evaluates the condition expression
 *   <li>{@link ReactiveRolloutEvaluationStep} — checks rollout bucket membership
 * </ol>
 *
 * <p>Returns the first {@link AccessDecision.Denied} encountered, or {@link AccessDecision.Allowed}
 * if all steps pass.
 */
public class ReactiveEndpointGateEvaluationPipeline {

  private final ReactiveEnabledEvaluationStep enabledStep;
  private final ReactiveScheduleEvaluationStep scheduleStep;
  private final ReactiveConditionEvaluationStep conditionStep;
  private final ReactiveRolloutEvaluationStep rolloutStep;

  /**
   * Creates a new {@code ReactiveEndpointGateEvaluationPipeline} with the fixed evaluation order.
   *
   * @param enabledStep the step that checks whether the gate is enabled
   * @param scheduleStep the step that checks the schedule
   * @param conditionStep the step that evaluates the condition expression
   * @param rolloutStep the step that checks rollout bucket membership
   */
  public ReactiveEndpointGateEvaluationPipeline(
      ReactiveEnabledEvaluationStep enabledStep,
      ReactiveScheduleEvaluationStep scheduleStep,
      ReactiveConditionEvaluationStep conditionStep,
      ReactiveRolloutEvaluationStep rolloutStep) {
    this.enabledStep = enabledStep;
    this.scheduleStep = scheduleStep;
    this.conditionStep = conditionStep;
    this.rolloutStep = rolloutStep;
  }

  /**
   * Evaluates all steps sequentially and returns the first denied decision or {@link
   * AccessDecision#allowed()} if all steps pass.
   *
   * <p>Short-circuits on the first {@link AccessDecision.Denied} result.
   *
   * @param context the evaluation context
   * @return a {@link Mono} emitting the access decision
   */
  public Mono<AccessDecision> evaluate(EvaluationContext context) {
    return Flux.just(enabledStep, scheduleStep, conditionStep, rolloutStep)
        .concatMap(step -> step.evaluate(context))
        .filter(AccessDecision.Denied.class::isInstance)
        .next()
        .defaultIfEmpty(AccessDecision.allowed());
  }
}
