package net.brightroom.endpointgate.spring.metrics.evaluation;

import java.time.Clock;
import java.util.Optional;
import net.brightroom.endpointgate.core.evaluation.ConditionEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.EnabledEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.RolloutEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.ScheduleEvaluationStep;

/**
 * Provides no-op evaluation steps used solely to satisfy the {@link
 * net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline} superclass
 * constructor. The decorator overrides {@code evaluate()} and delegates to the wrapped pipeline, so
 * these steps are never invoked.
 */
final class NoOpEvaluationSteps {

  private NoOpEvaluationSteps() {}

  /**
   * Returns a no-op {@link EnabledEvaluationStep}.
   *
   * @return a no-op enabled evaluation step
   */
  static EnabledEvaluationStep enabled() {
    return new EnabledEvaluationStep(gateId -> true);
  }

  /**
   * Returns a no-op {@link ScheduleEvaluationStep}.
   *
   * @return a no-op schedule evaluation step
   */
  static ScheduleEvaluationStep schedule() {
    return new ScheduleEvaluationStep(gateId -> Optional.empty(), Clock.systemDefaultZone());
  }

  /**
   * Returns a no-op {@link ConditionEvaluationStep}.
   *
   * @return a no-op condition evaluation step
   */
  static ConditionEvaluationStep condition() {
    return new ConditionEvaluationStep((expression, variables) -> true);
  }

  /**
   * Returns a no-op {@link RolloutEvaluationStep}.
   *
   * @return a no-op rollout evaluation step
   */
  static RolloutEvaluationStep rollout() {
    return new RolloutEvaluationStep((gateId, context, percentage) -> true);
  }
}
