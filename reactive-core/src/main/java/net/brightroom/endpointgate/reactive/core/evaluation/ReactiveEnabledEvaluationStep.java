package net.brightroom.endpointgate.reactive.core.evaluation;

import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import reactor.core.publisher.Mono;

/** Reactive evaluation step that checks whether the gate is enabled. */
public class ReactiveEnabledEvaluationStep implements ReactiveEvaluationStep {

  private final ReactiveEndpointGateProvider provider;

  /**
   * Creates a new {@code ReactiveEnabledEvaluationStep}.
   *
   * @param provider the provider used to check whether a gate is enabled
   */
  public ReactiveEnabledEvaluationStep(ReactiveEndpointGateProvider provider) {
    this.provider = provider;
  }

  @Override
  public Mono<AccessDecision> evaluate(EvaluationContext context) {
    return provider
        .isGateEnabled(context.gateId())
        .defaultIfEmpty(false)
        .map(
            enabled ->
                enabled
                    ? AccessDecision.allowed()
                    : AccessDecision.denied(context.gateId(), DeniedReason.DISABLED));
  }
}
