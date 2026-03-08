package net.brightroom.endpointgate.core.evaluation;

import java.util.Optional;

/**
 * SPI for a single step in the synchronous endpoint gate evaluation pipeline.
 *
 * <p>Implement this interface to add a custom evaluation step.
 */
public interface EvaluationStep {

  /**
   * Evaluates one step of the gate decision pipeline.
   *
   * @param context the evaluation context containing all inputs for this step
   * @return {@link Optional#empty()} if this step passes (proceed to the next step), or an {@link
   *     Optional} containing an {@link AccessDecision.Denied} if this step rejects the request
   */
  Optional<AccessDecision> evaluate(EvaluationContext context);
}
