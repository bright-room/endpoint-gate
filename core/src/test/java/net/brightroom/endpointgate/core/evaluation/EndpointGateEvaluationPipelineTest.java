package net.brightroom.endpointgate.core.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import org.junit.jupiter.api.Test;

class EndpointGateEvaluationPipelineTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();
  private static final EvaluationContext CTX =
      new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> null);

  private final EnabledEvaluationStep enabledStep = mock(EnabledEvaluationStep.class);
  private final ScheduleEvaluationStep scheduleStep = mock(ScheduleEvaluationStep.class);
  private final ConditionEvaluationStep conditionStep = mock(ConditionEvaluationStep.class);
  private final RolloutEvaluationStep rolloutStep = mock(RolloutEvaluationStep.class);

  @Test
  void evaluate_returnsAllowed_whenAllStepsPass() {
    when(enabledStep.evaluate(any())).thenReturn(Optional.empty());
    when(scheduleStep.evaluate(any())).thenReturn(Optional.empty());
    when(conditionStep.evaluate(any())).thenReturn(Optional.empty());
    when(rolloutStep.evaluate(any())).thenReturn(Optional.empty());

    EndpointGateEvaluationPipeline pipeline =
        new EndpointGateEvaluationPipeline(enabledStep, scheduleStep, conditionStep, rolloutStep);
    assertThat(pipeline.evaluate(CTX)).isInstanceOf(AccessDecision.Allowed.class);
  }

  @Test
  void evaluate_returnsFirstDenied_whenStepDenies() {
    AccessDecision expectedDenial = AccessDecision.denied("my-gate", DeniedReason.DISABLED);
    when(enabledStep.evaluate(any())).thenReturn(Optional.of(expectedDenial));

    EndpointGateEvaluationPipeline pipeline =
        new EndpointGateEvaluationPipeline(enabledStep, scheduleStep, conditionStep, rolloutStep);
    assertThat(pipeline.evaluate(CTX)).isEqualTo(expectedDenial);
  }

  @Test
  void evaluate_shortCircuits_afterFirstDenial() {
    AccessDecision denial = AccessDecision.denied("my-gate", DeniedReason.DISABLED);
    when(enabledStep.evaluate(any())).thenReturn(Optional.of(denial));

    EndpointGateEvaluationPipeline pipeline =
        new EndpointGateEvaluationPipeline(enabledStep, scheduleStep, conditionStep, rolloutStep);
    pipeline.evaluate(CTX);

    verify(scheduleStep, never()).evaluate(any());
    verify(conditionStep, never()).evaluate(any());
    verify(rolloutStep, never()).evaluate(any());
  }

  @Test
  void evaluate_executesStepsInOrder_enabled_schedule_condition_rollout() {
    when(enabledStep.evaluate(any())).thenReturn(Optional.empty());
    when(scheduleStep.evaluate(any())).thenReturn(Optional.empty());
    AccessDecision denial = AccessDecision.denied("my-gate", DeniedReason.CONDITION_NOT_MET);
    when(conditionStep.evaluate(any())).thenReturn(Optional.of(denial));

    EndpointGateEvaluationPipeline pipeline =
        new EndpointGateEvaluationPipeline(enabledStep, scheduleStep, conditionStep, rolloutStep);
    AccessDecision result = pipeline.evaluate(CTX);

    assertThat(result).isEqualTo(denial);
    verify(enabledStep).evaluate(any());
    verify(scheduleStep).evaluate(any());
    verify(conditionStep).evaluate(any());
    verify(rolloutStep, never()).evaluate(any());
  }
}
