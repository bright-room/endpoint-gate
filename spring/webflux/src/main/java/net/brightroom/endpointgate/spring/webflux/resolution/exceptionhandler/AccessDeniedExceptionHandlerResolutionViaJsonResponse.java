package net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.core.resolution.AccessDeniedResponseAttributes;
import net.brightroom.endpointgate.spring.core.resolution.ProblemDetailBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

class AccessDeniedExceptionHandlerResolutionViaJsonResponse
    implements AccessDeniedExceptionHandlerResolution {

  @Override
  public ResponseEntity<?> resolution(
      ServerHttpRequest request, EndpointGateAccessDeniedException e) {
    var body = ProblemDetailBuilder.build(request.getPath().value(), e);
    var status = AccessDeniedResponseAttributes.resolveStatus(e);
    var builder = ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON);
    String retryAfter = AccessDeniedResponseAttributes.formatRetryAfter(e);
    if (retryAfter != null) {
      builder = builder.header(HttpHeaders.RETRY_AFTER, retryAfter);
    }
    return builder.body(body);
  }

  AccessDeniedExceptionHandlerResolutionViaJsonResponse() {}
}
