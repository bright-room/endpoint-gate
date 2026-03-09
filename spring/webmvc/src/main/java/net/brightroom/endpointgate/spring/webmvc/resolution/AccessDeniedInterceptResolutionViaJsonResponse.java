package net.brightroom.endpointgate.spring.webmvc.resolution;

import jakarta.servlet.http.HttpServletRequest;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.core.resolution.AccessDeniedResponseAttributes;
import net.brightroom.endpointgate.spring.core.resolution.ProblemDetailBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class AccessDeniedInterceptResolutionViaJsonResponse implements AccessDeniedInterceptResolution {

  @Override
  public ResponseEntity<?> resolution(
      HttpServletRequest request, EndpointGateAccessDeniedException e) {
    var body = ProblemDetailBuilder.build(request.getRequestURI(), e);
    var status = AccessDeniedResponseAttributes.resolveStatus(e);
    var builder = ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON);
    String retryAfter = AccessDeniedResponseAttributes.formatRetryAfter(e);
    if (retryAfter != null) {
      builder = builder.header(HttpHeaders.RETRY_AFTER, retryAfter);
    }
    return builder.body(body);
  }

  AccessDeniedInterceptResolutionViaJsonResponse() {}
}
