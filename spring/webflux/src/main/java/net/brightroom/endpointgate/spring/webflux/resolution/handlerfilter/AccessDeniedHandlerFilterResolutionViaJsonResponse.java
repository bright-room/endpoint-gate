package net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.core.resolution.ProblemDetailBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class AccessDeniedHandlerFilterResolutionViaJsonResponse
    implements AccessDeniedHandlerFilterResolution {

  @Override
  public Mono<ServerResponse> resolve(ServerRequest request, EndpointGateAccessDeniedException e) {
    var body = ProblemDetailBuilder.build(request.path(), e);
    return ServerResponse.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .bodyValue(body);
  }

  AccessDeniedHandlerFilterResolutionViaJsonResponse() {}
}
