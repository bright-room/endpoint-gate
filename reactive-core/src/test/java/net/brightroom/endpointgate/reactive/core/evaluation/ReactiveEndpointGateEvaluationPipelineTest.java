package net.brightroom.endpointgate.reactive.core.evaluation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveEndpointGateEvaluationPipelineTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();
  private static final EvaluationContext CTX =
      new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> null);

  private ReactiveEndpointGateEvaluationPipeline pipelineAllAllowed() {
    var enabledStep = mock(ReactiveEnabledEvaluationStep.class);
    var scheduleStep = mock(ReactiveScheduleEvaluationStep.class);
    var conditionStep = mock(ReactiveConditionEvaluationStep.class);
    var rolloutStep = mock(ReactiveRolloutEvaluationStep.class);
    when(enabledStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(scheduleStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(conditionStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(rolloutStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    return new ReactiveEndpointGateEvaluationPipeline(
        enabledStep, scheduleStep, conditionStep, rolloutStep);
  }

  @Test
  void evaluate_returnsAllowed_whenAllStepsPass() {
    StepVerifier.create(pipelineAllAllowed().evaluate(CTX))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
  }

  @Test
  void evaluate_returnsFirstDenied_whenEnabledStepDenies() {
    AccessDecision denial = AccessDecision.denied("my-gate", DeniedReason.DISABLED);
    var enabledStep = mock(ReactiveEnabledEvaluationStep.class);
    var scheduleStep = mock(ReactiveScheduleEvaluationStep.class);
    var conditionStep = mock(ReactiveConditionEvaluationStep.class);
    var rolloutStep = mock(ReactiveRolloutEvaluationStep.class);
    when(enabledStep.evaluate(any())).thenReturn(Mono.just(denial));
    when(scheduleStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(conditionStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(rolloutStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    var pipeline =
        new ReactiveEndpointGateEvaluationPipeline(
            enabledStep, scheduleStep, conditionStep, rolloutStep);

    StepVerifier.create(pipeline.evaluate(CTX)).expectNext(denial).verifyComplete();
  }

  @Test
  void evaluate_returnsFirstDenied_whenScheduleStepDenies() {
    AccessDecision denial = AccessDecision.denied("my-gate", DeniedReason.SCHEDULE_INACTIVE);
    var enabledStep = mock(ReactiveEnabledEvaluationStep.class);
    var scheduleStep = mock(ReactiveScheduleEvaluationStep.class);
    var conditionStep = mock(ReactiveConditionEvaluationStep.class);
    var rolloutStep = mock(ReactiveRolloutEvaluationStep.class);
    when(enabledStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(scheduleStep.evaluate(any())).thenReturn(Mono.just(denial));
    when(conditionStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(rolloutStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    var pipeline =
        new ReactiveEndpointGateEvaluationPipeline(
            enabledStep, scheduleStep, conditionStep, rolloutStep);

    StepVerifier.create(pipeline.evaluate(CTX)).expectNext(denial).verifyComplete();
  }

  @Test
  void evaluate_returnsFirstDenied_whenConditionStepDenies() {
    AccessDecision denial = AccessDecision.denied("my-gate", DeniedReason.CONDITION_NOT_MET);
    var enabledStep = mock(ReactiveEnabledEvaluationStep.class);
    var scheduleStep = mock(ReactiveScheduleEvaluationStep.class);
    var conditionStep = mock(ReactiveConditionEvaluationStep.class);
    var rolloutStep = mock(ReactiveRolloutEvaluationStep.class);
    when(enabledStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(scheduleStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    when(conditionStep.evaluate(any())).thenReturn(Mono.just(denial));
    when(rolloutStep.evaluate(any())).thenReturn(Mono.just(AccessDecision.allowed()));
    var pipeline =
        new ReactiveEndpointGateEvaluationPipeline(
            enabledStep, scheduleStep, conditionStep, rolloutStep);

    StepVerifier.create(pipeline.evaluate(CTX)).expectNext(denial).verifyComplete();
  }
}
