package net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler;

import java.nio.charset.StandardCharsets;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.core.resolution.AccessDeniedResponseAttributes;
import net.brightroom.endpointgate.spring.core.resolution.HtmlResponseBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

class AccessDeniedExceptionHandlerResolutionViaHtmlResponse
    implements AccessDeniedExceptionHandlerResolution {

  private static final MediaType TEXT_HTML_UTF8 =
      new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);

  @Override
  public ResponseEntity<?> resolution(
      @SuppressWarnings("unused") ServerHttpRequest request, EndpointGateAccessDeniedException e) {
    var status = AccessDeniedResponseAttributes.resolveStatus(e);
    var builder = ResponseEntity.status(status).contentType(TEXT_HTML_UTF8);
    String retryAfter = AccessDeniedResponseAttributes.formatRetryAfter(e);
    if (retryAfter != null) {
      builder = builder.header(HttpHeaders.RETRY_AFTER, retryAfter);
    }
    return builder.body(HtmlResponseBuilder.buildHtml(e));
  }

  AccessDeniedExceptionHandlerResolutionViaHtmlResponse() {}
}
