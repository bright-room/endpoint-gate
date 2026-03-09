package net.brightroom.endpointgate.spring.webmvc.resolution;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import net.brightroom.endpointgate.spring.core.resolution.HtmlResponseBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * An {@link AccessDeniedInterceptResolution} implementation that returns a fixed HTML response.
 *
 * <p>This implementation returns a {@code ResponseEntity<String>} with {@code text/html} content
 * type, which Spring MVC writes using {@code StringHttpMessageConverter}. The converter can only
 * write the response when the client's {@code Accept} header includes {@code text/html} or {@code
 * text/*}. If the client sends {@code Accept: application/json} only, Spring MVC will return {@code
 * 406 Not Acceptable} instead of the intended HTML response.
 *
 * <p>For full control over the denied response regardless of the {@code Accept} header, define a
 * custom {@code @ControllerAdvice} that handles {@link EndpointGateAccessDeniedException}.
 */
class AccessDeniedInterceptResolutionViaHtmlResponse implements AccessDeniedInterceptResolution {

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
            .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8));
    if (e instanceof EndpointGateScheduleInactiveException scheduleException
        && scheduleException.retryAfter() != null) {
      builder =
          builder.header(
              HttpHeaders.RETRY_AFTER,
              DateTimeFormatter.RFC_1123_DATE_TIME.format(
                  scheduleException.retryAfter().atZone(ZoneOffset.UTC)));
    }
    return builder.body(HtmlResponseBuilder.buildHtml(e));
  }

  AccessDeniedInterceptResolutionViaHtmlResponse() {}
}
