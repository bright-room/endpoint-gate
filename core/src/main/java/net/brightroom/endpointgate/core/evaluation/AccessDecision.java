package net.brightroom.endpointgate.core.evaluation;

/**
 * Represents the outcome of an endpoint gate evaluation pipeline.
 *
 * <p>Either {@link Allowed} (proceed) or {@link Denied} (block with a reason).
 */
public sealed interface AccessDecision {

  /** Indicates that access is allowed. */
  record Allowed() implements AccessDecision {}

  /** Indicates that access is denied, with the gate identifier and reason. */
  record Denied(String gateId, DeniedReason reason) implements AccessDecision {}

  /** Reason for denying access in the evaluation pipeline. */
  enum DeniedReason {
    /** The gate is disabled. */
    DISABLED,
    /** The gate has a schedule that is not currently active. */
    SCHEDULE_INACTIVE,
    /** The condition evaluated to false. */
    CONDITION_NOT_MET,
    /** The request is outside the rollout bucket. */
    ROLLOUT_EXCLUDED
  }

  /**
   * Returns an {@link Allowed} decision.
   *
   * @return a new {@code Allowed} instance
   */
  static AccessDecision allowed() {
    return new Allowed();
  }

  /**
   * Returns a {@link Denied} decision.
   *
   * @param gateId the gate identifier that was denied
   * @param reason the reason for denial
   * @return a new {@code Denied} instance
   */
  static AccessDecision denied(String gateId, DeniedReason reason) {
    return new Denied(gateId, reason);
  }
}
