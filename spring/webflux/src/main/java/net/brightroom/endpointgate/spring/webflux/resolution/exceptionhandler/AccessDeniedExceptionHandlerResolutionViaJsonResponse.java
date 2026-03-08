package net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.core.resolution.ProblemDetailBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

class AccessDeniedExceptionHandlerResolutionViaJsonResponse
    implements AccessDeniedExceptionHandlerResolution {

  @Override
  public ResponseEntity<?> resolution(
      ServerHttpRequest request, EndpointGateAccessDeniedException e) {
    var body = ProblemDetailBuilder.build(request.getPath().value(), e);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(body);
  }

  AccessDeniedExceptionHandlerResolutionViaJsonResponse() {}
}
