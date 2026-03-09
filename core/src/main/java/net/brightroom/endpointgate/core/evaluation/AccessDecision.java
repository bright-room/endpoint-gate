package net.brightroom.endpointgate.core.evaluation;

import java.time.Instant;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import org.jspecify.annotations.Nullable;

/**
 * Represents the outcome of an endpoint gate evaluation pipeline.
 *
 * <p>Either {@link Allowed} (proceed) or {@link Denied} (block with a reason).
 */
public sealed interface AccessDecision {

  /** Indicates that access is allowed. */
  record Allowed() implements AccessDecision {}

  /**
   * Indicates that access is denied, with the gate identifier, reason, and optional retry-after
   * time.
   *
   * @param gateId the gate identifier that was denied
   * @param reason the reason for denial
   * @param retryAfter the time after which the client may retry, or {@code null} if not applicable
   */
  record Denied(String gateId, DeniedReason reason, @Nullable Instant retryAfter)
      implements AccessDecision {

    /**
     * Converts this denied decision to the appropriate exception type.
     *
     * <p>If the reason is {@link DeniedReason#SCHEDULE_INACTIVE}, returns a {@link
     * EndpointGateScheduleInactiveException}. Otherwise, returns a {@link
     * EndpointGateAccessDeniedException}.
     *
     * @return the exception corresponding to this denial
     */
    public EndpointGateAccessDeniedException toException() {
      if (reason == DeniedReason.SCHEDULE_INACTIVE) {
        return new EndpointGateScheduleInactiveException(gateId, retryAfter);
      }
      return new EndpointGateAccessDeniedException(gateId);
    }
  }

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
   * Returns a {@link Denied} decision with no retry-after time.
   *
   * @param gateId the gate identifier that was denied
   * @param reason the reason for denial
   * @return a new {@code Denied} instance
   */
  static AccessDecision denied(String gateId, DeniedReason reason) {
    return new Denied(gateId, reason, null);
  }

  /**
   * Returns a {@link Denied} decision with a retry-after time.
   *
   * @param gateId the gate identifier that was denied
   * @param reason the reason for denial
   * @param retryAfter the time after which the client may retry, or {@code null} if not applicable
   * @return a new {@code Denied} instance
   */
  static AccessDecision denied(String gateId, DeniedReason reason, @Nullable Instant retryAfter) {
    return new Denied(gateId, reason, retryAfter);
  }
}
