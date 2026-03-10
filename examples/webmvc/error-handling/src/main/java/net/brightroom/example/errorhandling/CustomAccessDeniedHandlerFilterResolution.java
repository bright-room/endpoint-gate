package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.webmvc.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * C-3: Custom AccessDeniedHandlerFilterResolution for Functional Endpoint path.
 *
 * <p>Replaces the default bean via @ConditionalOnMissingBean. Returns a custom JSON error response
 * when an endpoint gate denies access through functional endpoints.
 */
@Component
@Profile("functional-error")
public class CustomAccessDeniedHandlerFilterResolution
    implements AccessDeniedHandlerFilterResolution {

  @Override
  public ServerResponse resolve(ServerRequest request, EndpointGateAccessDeniedException e) {
    String body =
        String.format(
            "{\"error\":\"access_denied\",\"gate\":\"%s\",\"path\":\"%s\"}",
            e.gateId(), request.path());
    ServerResponse.BodyBuilder builder =
        ServerResponse.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON);
    return builder.body(body);
  }
}
