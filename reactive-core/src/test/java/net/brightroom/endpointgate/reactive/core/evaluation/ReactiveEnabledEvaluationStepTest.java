package net.brightroom.endpointgate.reactive.core.evaluation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveEnabledEvaluationStepTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();
  private static final EvaluationContext CTX =
      new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> null);

  private final ReactiveEndpointGateProvider provider = mock(ReactiveEndpointGateProvider.class);
  private final ReactiveEnabledEvaluationStep step = new ReactiveEnabledEvaluationStep(provider);

  @Test
  void evaluate_returnsAllowed_whenGateEnabled() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    StepVerifier.create(step.evaluate(CTX))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
  }

  @Test
  void evaluate_returnsDenied_whenGateDisabled() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(false));
    StepVerifier.create(step.evaluate(CTX))
        .expectNextMatches(
            d ->
                d instanceof AccessDecision.Denied denied
                    && denied.gateId().equals("my-gate")
                    && denied.reason() == DeniedReason.DISABLED)
        .verifyComplete();
  }

  @Test
  void evaluate_returnsDenied_whenProviderReturnsEmpty() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.empty());
    StepVerifier.create(step.evaluate(CTX))
        .expectNextMatches(
            d ->
                d instanceof AccessDecision.Denied denied
                    && denied.reason() == DeniedReason.DISABLED)
        .verifyComplete();
  }
}
