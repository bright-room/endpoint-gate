package net.brightroom.endpointgate.reactive.core.evaluation;

import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import reactor.core.publisher.Mono;

/**
 * SPI for a single step in the reactive endpoint gate evaluation pipeline.
 *
 * <p>Implement this interface to add a custom reactive evaluation step.
 */
public interface ReactiveEvaluationStep {

  /**
   * Evaluates one step of the reactive gate decision pipeline.
   *
   * @param context the evaluation context containing all inputs for this step
   * @return a {@link Mono} emitting {@link AccessDecision.Allowed} if this step passes, or {@link
   *     AccessDecision.Denied} if this step rejects the request
   */
  Mono<AccessDecision> evaluate(EvaluationContext context);
}
