package net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import net.brightroom.endpointgate.spring.core.resolution.ProblemDetailBuilder;
import org.springframework.http.HttpHeaders;
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
    HttpStatus status;
    if (e instanceof EndpointGateScheduleInactiveException) {
      status = HttpStatus.SERVICE_UNAVAILABLE;
    } else {
      status = HttpStatus.FORBIDDEN;
    }
    var builder = ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON);
    if (e instanceof EndpointGateScheduleInactiveException scheduleException
        && scheduleException.retryAfter() != null) {
      builder =
          builder.header(
              HttpHeaders.RETRY_AFTER,
              DateTimeFormatter.RFC_1123_DATE_TIME.format(
                  scheduleException.retryAfter().atZone(ZoneOffset.UTC)));
    }
    return builder.body(body);
  }

  AccessDeniedExceptionHandlerResolutionViaJsonResponse() {}
}
