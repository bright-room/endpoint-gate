package net.brightroom.endpointgate.spring.webmvc.resolution;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import net.brightroom.endpointgate.spring.core.resolution.ProblemDetailBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class AccessDeniedInterceptResolutionViaJsonResponse implements AccessDeniedInterceptResolution {

  @Override
  public ResponseEntity<?> resolution(
      HttpServletRequest request, EndpointGateAccessDeniedException e) {
    var body = ProblemDetailBuilder.build(request.getRequestURI(), e);
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

  AccessDeniedInterceptResolutionViaJsonResponse() {}
}
