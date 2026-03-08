package net.brightroom.endpointgate.spring.core.resolution;

import java.net.URI;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
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
   * @param requestPath the path of the denied request
   * @param e the exception that triggered the denial
   * @return a populated {@link ProblemDetail} with status 403
   */
  public static ProblemDetail build(String requestPath, EndpointGateAccessDeniedException e) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problemDetail.setType(
        URI.create("https://github.com/bright-room/endpoint-gate#response-types"));
    problemDetail.setTitle("Endpoint gate access denied");
    problemDetail.setDetail(e.getMessage());
    problemDetail.setInstance(URI.create(requestPath));
    return problemDetail;
  }
}
