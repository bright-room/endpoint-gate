package net.brightroom.endpointgate.core.exception;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * Thrown when access to a gate-protected endpoint is denied because the gate's schedule is not
 * currently active.
 *
 * <p>This is a subclass of {@link EndpointGateAccessDeniedException}, so existing
 * {@code @ControllerAdvice} handlers that catch {@code EndpointGateAccessDeniedException} will also
 * catch this exception.
 *
 * <p>The {@link #retryAfter()} method returns the schedule start time as an {@link Instant}, which
 * can be used to populate the HTTP {@code Retry-After} header. If the schedule has no configured
 * start time (only an end time), {@link #retryAfter()} returns {@code null}.
 */
public class EndpointGateScheduleInactiveException extends EndpointGateAccessDeniedException {

  private static final long serialVersionUID = 1L;

  private final @Nullable Instant retryAfter;

  /**
   * Constructor.
   *
   * @param gateId the identifier of the gate that is not available
   * @param retryAfter the schedule start time as an {@link Instant}, or {@code null} if the
   *     schedule has no start time configured
   */
  public EndpointGateScheduleInactiveException(String gateId, @Nullable Instant retryAfter) {
    super(gateId, buildMessage(gateId, retryAfter));
    this.retryAfter = retryAfter;
  }

  /**
   * Returns the schedule start time as an {@link Instant}, or {@code null} if the schedule has no
   * start time configured.
   *
   * <p>This value can be used to populate the HTTP {@code Retry-After} response header.
   *
   * @return the retry-after instant, or {@code null}
   */
  public @Nullable Instant retryAfter() {
    return retryAfter;
  }

  private static String buildMessage(String gateId, @Nullable Instant retryAfter) {
    if (retryAfter == null) {
      return String.format("Gate '%s' is not available", gateId);
    }
    return String.format("Gate '%s' is not available until %s", gateId, retryAfter);
  }
}
