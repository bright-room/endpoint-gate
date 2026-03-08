package net.brightroom.endpointgate.reactive.core.evaluation;

import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.rollout.ReactiveRolloutStrategy;
import reactor.core.publisher.Mono;

/** Reactive evaluation step that checks whether the request is within the rollout bucket. */
public class ReactiveRolloutEvaluationStep implements ReactiveEvaluationStep {

  private final ReactiveRolloutStrategy rolloutStrategy;

  /**
   * Creates a new {@code ReactiveRolloutEvaluationStep}.
   *
   * @param rolloutStrategy the strategy used to determine rollout bucket membership
   */
  public ReactiveRolloutEvaluationStep(ReactiveRolloutStrategy rolloutStrategy) {
    this.rolloutStrategy = rolloutStrategy;
  }

  @Override
  public Mono<AccessDecision> evaluate(EvaluationContext context) {
    if (context.rolloutPercentage() >= 100) {
      return Mono.just(AccessDecision.allowed());
    }
    EndpointGateContext gateContext = context.gateContextSupplier().get();
    if (gateContext == null) {
      return Mono.just(AccessDecision.allowed()); // fail-open: no context available
    }
    return rolloutStrategy
        .isInRollout(context.gateId(), gateContext, context.rolloutPercentage())
        .map(
            inRollout -> {
              if (inRollout) {
                return AccessDecision.allowed();
              }
              return AccessDecision.denied(context.gateId(), DeniedReason.ROLLOUT_EXCLUDED);
            });
  }
}
