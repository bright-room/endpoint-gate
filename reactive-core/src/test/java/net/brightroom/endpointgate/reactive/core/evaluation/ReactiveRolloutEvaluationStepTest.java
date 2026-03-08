package net.brightroom.endpointgate.reactive.core.evaluation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.rollout.ReactiveRolloutStrategy;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveRolloutEvaluationStepTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();
  private static final EndpointGateContext GATE_CTX = new EndpointGateContext("user-1");

  private final ReactiveRolloutStrategy strategy = mock(ReactiveRolloutStrategy.class);
  private final ReactiveRolloutEvaluationStep step = new ReactiveRolloutEvaluationStep(strategy);

  @Test
  void evaluate_returnsAllowed_whenRolloutIs100() {
    EvaluationContext ctx = new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> GATE_CTX);
    StepVerifier.create(step.evaluate(ctx))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
    verifyNoInteractions(strategy);
  }

  @Test
  void evaluate_returnsAllowed_whenGateContextIsNull_failOpen() {
    EvaluationContext ctx = new EvaluationContext("my-gate", "", 50, EMPTY_VARS, () -> null);
    StepVerifier.create(step.evaluate(ctx))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
    verifyNoInteractions(strategy);
  }

  @Test
  void evaluate_returnsAllowed_whenStrategyReturnsTrue() {
    when(strategy.isInRollout("my-gate", GATE_CTX, 50)).thenReturn(Mono.just(true));
    EvaluationContext ctx = new EvaluationContext("my-gate", "", 50, EMPTY_VARS, () -> GATE_CTX);
    StepVerifier.create(step.evaluate(ctx))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
  }

  @Test
  void evaluate_returnsDenied_whenStrategyReturnsFalse() {
    when(strategy.isInRollout("my-gate", GATE_CTX, 50)).thenReturn(Mono.just(false));
    EvaluationContext ctx = new EvaluationContext("my-gate", "", 50, EMPTY_VARS, () -> GATE_CTX);
    StepVerifier.create(step.evaluate(ctx))
        .expectNextMatches(
            d ->
                d instanceof AccessDecision.Denied denied
                    && denied.gateId().equals("my-gate")
                    && denied.reason() == DeniedReason.ROLLOUT_EXCLUDED)
        .verifyComplete();
  }
}
