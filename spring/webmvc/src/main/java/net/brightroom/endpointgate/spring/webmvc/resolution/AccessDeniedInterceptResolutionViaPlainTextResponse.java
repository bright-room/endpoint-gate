package net.brightroom.endpointgate.spring.webmvc.resolution;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class AccessDeniedInterceptResolutionViaPlainTextResponse
    implements AccessDeniedInterceptResolution {

  @Override
  public ResponseEntity<?> resolution(
      @SuppressWarnings("unused") HttpServletRequest request, EndpointGateAccessDeniedException e) {
    HttpStatus status;
    if (e instanceof EndpointGateScheduleInactiveException) {
      status = HttpStatus.SERVICE_UNAVAILABLE;
    } else {
      status = HttpStatus.FORBIDDEN;
    }
    var builder =
        ResponseEntity.status(status)
            .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8));
    if (e instanceof EndpointGateScheduleInactiveException scheduleException
        && scheduleException.retryAfter() != null) {
      builder =
          builder.header(
              HttpHeaders.RETRY_AFTER,
              DateTimeFormatter.RFC_1123_DATE_TIME.format(
                  scheduleException.retryAfter().atZone(ZoneOffset.UTC)));
    }
    return builder.body(e.getMessage());
  }

  AccessDeniedInterceptResolutionViaPlainTextResponse() {}
}
