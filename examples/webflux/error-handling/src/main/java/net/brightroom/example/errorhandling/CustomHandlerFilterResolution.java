package net.brightroom.example.errorhandling;

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

@Component
@Profile("functional-error")
public class CustomHandlerFilterResolution implements AccessDeniedHandlerFilterResolution {

  @Override
  public Mono<ServerResponse> resolve(ServerRequest request, EndpointGateAccessDeniedException e) {
    return ServerResponse.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            Map.of(
                "error", "Feature Unavailable",
                "gate", e.gateId(),
                "message", "This feature is currently disabled."));
  }
}
