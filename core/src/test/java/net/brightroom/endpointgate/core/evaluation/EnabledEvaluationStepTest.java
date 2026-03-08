package net.brightroom.endpointgate.core.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import org.junit.jupiter.api.Test;

class EnabledEvaluationStepTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();
  private static final EvaluationContext CTX =
      new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> null);

  private final EndpointGateProvider provider = mock(EndpointGateProvider.class);
  private final EnabledEvaluationStep step = new EnabledEvaluationStep(provider);

  @Test
  void evaluate_returnsEmpty_whenGateEnabled() {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    Optional<AccessDecision> result = step.evaluate(CTX);
    assertThat(result).isEmpty();
  }

  @Test
  void evaluate_returnsDenied_whenGateDisabled() {
    when(provider.isGateEnabled("my-gate")).thenReturn(false);
    Optional<AccessDecision> result = step.evaluate(CTX);
    assertThat(result).isPresent();
    assertThat(result.get()).isInstanceOf(AccessDecision.Denied.class);
    AccessDecision.Denied denied = (AccessDecision.Denied) result.get();
    assertThat(denied.gateId()).isEqualTo("my-gate");
    assertThat(denied.reason()).isEqualTo(DeniedReason.DISABLED);
  }
}
