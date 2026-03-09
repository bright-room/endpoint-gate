package net.brightroom.endpointgate.spring.webmvc.resolution;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.core.resolution.AccessDeniedResponseAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class AccessDeniedInterceptResolutionViaPlainTextResponse
    implements AccessDeniedInterceptResolution {

  @Override
  public ResponseEntity<?> resolution(
      @SuppressWarnings("unused") HttpServletRequest request, EndpointGateAccessDeniedException e) {
    var status = AccessDeniedResponseAttributes.resolveStatus(e);
    var builder =
        ResponseEntity.status(status)
            .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8));
    String retryAfter = AccessDeniedResponseAttributes.formatRetryAfter(e);
    if (retryAfter != null) {
      builder = builder.header(HttpHeaders.RETRY_AFTER, retryAfter);
    }
    return builder.body(e.getMessage());
  }

  AccessDeniedInterceptResolutionViaPlainTextResponse() {}
}
