package net.brightroom.endpointgate.spring.core.resolution;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

/** Utility for resolving HTTP response attributes for denied endpoint gate access. */
public final class AccessDeniedResponseAttributes {

  private AccessDeniedResponseAttributes() {}

  /**
   * Resolves the HTTP status for a denied endpoint gate access.
   *
   * @param e the exception that triggered the denial
   * @return {@link HttpStatus#SERVICE_UNAVAILABLE} for schedule-inactive denials, {@link
   *     HttpStatus#FORBIDDEN} otherwise
   */
  public static HttpStatus resolveStatus(EndpointGateAccessDeniedException e) {
    if (e instanceof EndpointGateScheduleInactiveException) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    }
    return HttpStatus.FORBIDDEN;
  }

  /**
   * Formats the {@code Retry-After} header value for a denied endpoint gate access.
   *
   * @param e the exception that triggered the denial
   * @return the RFC 1123 formatted {@code Retry-After} header value, or {@code null} if not
   *     applicable
   */
  public static @Nullable String formatRetryAfter(EndpointGateAccessDeniedException e) {
    if (!(e instanceof EndpointGateScheduleInactiveException scheduleException)) {
      return null;
    }
    Instant retryAfter = scheduleException.retryAfter();
    if (retryAfter == null) {
      return null;
    }
    ZonedDateTime utcRetryAfter = retryAfter.atZone(ZoneOffset.UTC);
    DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
    return formatter.format(utcRetryAfter);
  }
}
