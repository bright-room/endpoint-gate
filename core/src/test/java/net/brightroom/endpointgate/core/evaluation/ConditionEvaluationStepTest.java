package net.brightroom.endpointgate.core.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.condition.EndpointGateConditionEvaluator;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import org.junit.jupiter.api.Test;

class ConditionEvaluationStepTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();

  private final EndpointGateConditionEvaluator evaluator =
      mock(EndpointGateConditionEvaluator.class);
  private final ConditionEvaluationStep step = new ConditionEvaluationStep(evaluator);

  @Test
  void evaluate_returnsEmpty_whenConditionIsEmpty() {
    EvaluationContext ctx = new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> null);
    assertThat(step.evaluate(ctx)).isEmpty();
    verifyNoInteractions(evaluator);
  }

  @Test
  void evaluate_returnsEmpty_whenConditionPasses() {
    when(evaluator.evaluate(eq("headers['X-Beta'] != null"), any())).thenReturn(true);
    EvaluationContext ctx =
        new EvaluationContext("my-gate", "headers['X-Beta'] != null", 100, EMPTY_VARS, () -> null);
    assertThat(step.evaluate(ctx)).isEmpty();
  }

  @Test
  void evaluate_returnsDenied_whenConditionFails() {
    when(evaluator.evaluate(eq("headers['X-Beta'] != null"), any())).thenReturn(false);
    EvaluationContext ctx =
        new EvaluationContext("my-gate", "headers['X-Beta'] != null", 100, EMPTY_VARS, () -> null);

    Optional<AccessDecision> result = step.evaluate(ctx);
    assertThat(result).isPresent();
    AccessDecision.Denied denied = (AccessDecision.Denied) result.get();
    assertThat(denied.gateId()).isEqualTo("my-gate");
    assertThat(denied.reason()).isEqualTo(DeniedReason.CONDITION_NOT_MET);
  }
}
