package net.brightroom.example.errorhandling.functional;

import java.util.Map;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * C-3: Custom AccessDeniedHandlerFilterResolution for Functional Endpoint path.
 *
 * <p>Replaces the default bean via @ConditionalOnMissingBean. Returns a custom JSON error response
 * when an endpoint gate denies access through functional endpoints.
 */
@Component
@Profile("custom-filter-resolution")
public class CustomAccessDeniedHandlerFilterResolution
    implements AccessDeniedHandlerFilterResolution {

  @Override
  public Mono<ServerResponse> resolve(ServerRequest request, EndpointGateAccessDeniedException e) {
    return ServerResponse.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            Map.of(
                "error", "gate_disabled",
                "gate", e.gateId(),
                "message", "This gate is not available via functional endpoint."));
  }
}
