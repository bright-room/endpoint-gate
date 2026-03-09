package net.brightroom.endpointgate.spring.core.resolution;

import java.net.URI;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Utility for building RFC 7807 {@link ProblemDetail} responses for endpoint gate access denial.
 */
public final class ProblemDetailBuilder {

  private ProblemDetailBuilder() {}

  /**
   * Builds a {@link ProblemDetail} for a denied endpoint gate access.
   *
   * <p>If the exception is a {@link EndpointGateScheduleInactiveException}, the response uses
   * status 503 and a title indicating temporary unavailability. Otherwise, status 403 is used.
   *
   * @param requestPath the path of the denied request
   * @param e the exception that triggered the denial
   * @return a populated {@link ProblemDetail}
   */
  public static ProblemDetail build(String requestPath, EndpointGateAccessDeniedException e) {
    HttpStatus status = AccessDeniedResponseAttributes.resolveStatus(e);
    String title = resolveTitle(e);
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(
        URI.create("https://github.com/bright-room/endpoint-gate#response-types"));
    problemDetail.setTitle(title);
    problemDetail.setDetail(e.getMessage());
    problemDetail.setInstance(URI.create(requestPath));
    return problemDetail;
  }

  private static String resolveTitle(EndpointGateAccessDeniedException e) {
    if (e instanceof EndpointGateScheduleInactiveException) {
      return "Endpoint gate temporarily unavailable";
    }
    return "Endpoint gate access denied";
  }
}
