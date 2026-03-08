package net.brightroom.endpointgate.core.rollout;

import net.brightroom.endpointgate.core.context.EndpointGateContext;

/**
 * Strategy for determining whether a given context is within the rollout bucket.
 *
 * <p>Implementations receive the gate identifier, context, and rollout percentage, and return
 * whether the request/user should be included in the rollout.
 */
public interface RolloutStrategy {

  /**
   * Determines whether the given context should be included in the rollout.
   *
   * @param gateId the gate identifier
   * @param context the context containing the user/request identifier
   * @param percentage the rollout percentage (0–100)
   * @return {@code true} if the context is within the rollout bucket
   */
  boolean isInRollout(String gateId, EndpointGateContext context, int percentage);
}
