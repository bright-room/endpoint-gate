package net.brightroom.endpointgate.spring.metrics.evaluation;

import java.time.Clock;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveConditionEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEnabledEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveRolloutEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveScheduleEvaluationStep;
import reactor.core.publisher.Mono;

/**
 * Provides no-op reactive evaluation steps used solely to satisfy the {@link
 * net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline}
 * superclass constructor. The decorator overrides {@code evaluate()} and delegates to the wrapped
 * pipeline, so these steps are never invoked.
 */
final class NoOpReactiveEvaluationSteps {

  private NoOpReactiveEvaluationSteps() {}

  /**
   * Returns a no-op {@link ReactiveEnabledEvaluationStep}.
   *
   * @return a no-op reactive enabled evaluation step
   */
  static ReactiveEnabledEvaluationStep enabled() {
    return new ReactiveEnabledEvaluationStep(gateId -> Mono.just(true));
  }

  /**
   * Returns a no-op {@link ReactiveScheduleEvaluationStep}.
   *
   * @return a no-op reactive schedule evaluation step
   */
  static ReactiveScheduleEvaluationStep schedule() {
    return new ReactiveScheduleEvaluationStep(gateId -> Mono.empty(), Clock.systemDefaultZone());
  }

  /**
   * Returns a no-op {@link ReactiveConditionEvaluationStep}.
   *
   * @return a no-op reactive condition evaluation step
   */
  static ReactiveConditionEvaluationStep condition() {
    return new ReactiveConditionEvaluationStep((expression, variables) -> Mono.just(true));
  }

  /**
   * Returns a no-op {@link ReactiveRolloutEvaluationStep}.
   *
   * @return a no-op reactive rollout evaluation step
   */
  static ReactiveRolloutEvaluationStep rollout() {
    return new ReactiveRolloutEvaluationStep((gateId, context, percentage) -> Mono.just(true));
  }
}
