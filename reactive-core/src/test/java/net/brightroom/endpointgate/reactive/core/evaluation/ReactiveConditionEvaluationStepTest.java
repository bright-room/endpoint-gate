package net.brightroom.endpointgate.reactive.core.evaluation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.condition.ReactiveEndpointGateConditionEvaluator;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveConditionEvaluationStepTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();

  private final ReactiveEndpointGateConditionEvaluator evaluator =
      mock(ReactiveEndpointGateConditionEvaluator.class);
  private final ReactiveConditionEvaluationStep step =
      new ReactiveConditionEvaluationStep(evaluator);

  @Test
  void evaluate_returnsAllowed_whenConditionIsEmpty() {
    EvaluationContext ctx = new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> null);
    StepVerifier.create(step.evaluate(ctx))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
    verifyNoInteractions(evaluator);
  }

  @Test
  void evaluate_returnsAllowed_whenConditionPasses() {
    when(evaluator.evaluate(eq("headers['X-Beta'] != null"), any())).thenReturn(Mono.just(true));
    EvaluationContext ctx =
        new EvaluationContext("my-gate", "headers['X-Beta'] != null", 100, EMPTY_VARS, () -> null);
    StepVerifier.create(step.evaluate(ctx))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
  }

  @Test
  void evaluate_returnsDenied_whenConditionFails() {
    when(evaluator.evaluate(eq("headers['X-Beta'] != null"), any())).thenReturn(Mono.just(false));
    EvaluationContext ctx =
        new EvaluationContext("my-gate", "headers['X-Beta'] != null", 100, EMPTY_VARS, () -> null);
    StepVerifier.create(step.evaluate(ctx))
        .expectNextMatches(
            d ->
                d instanceof AccessDecision.Denied denied
                    && denied.gateId().equals("my-gate")
                    && denied.reason() == DeniedReason.CONDITION_NOT_MET)
        .verifyComplete();
  }
}
