package net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import net.brightroom.endpointgate.spring.core.resolution.HtmlResponseBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class AccessDeniedHandlerFilterResolutionViaHtmlResponse
    implements AccessDeniedHandlerFilterResolution {

  private static final MediaType TEXT_HTML_UTF8 =
      new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);

  /**
   * {@inheritDoc}
   *
   * <p>Note: the {@code request} parameter is not used in this implementation.
   */
  @Override
  public Mono<ServerResponse> resolve(ServerRequest request, EndpointGateAccessDeniedException e) {
    HttpStatus status;
    if (e instanceof EndpointGateScheduleInactiveException) {
      status = HttpStatus.SERVICE_UNAVAILABLE;
    } else {
      status = HttpStatus.FORBIDDEN;
    }
    var builder = ServerResponse.status(status).contentType(TEXT_HTML_UTF8);
    if (e instanceof EndpointGateScheduleInactiveException scheduleException
        && scheduleException.retryAfter() != null) {
      builder =
          builder.header(
              HttpHeaders.RETRY_AFTER,
              DateTimeFormatter.RFC_1123_DATE_TIME.format(
                  scheduleException.retryAfter().atZone(ZoneOffset.UTC)));
    }
    return builder.bodyValue(HtmlResponseBuilder.buildHtml(e));
  }

  AccessDeniedHandlerFilterResolutionViaHtmlResponse() {}
}
