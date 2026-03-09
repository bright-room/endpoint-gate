package net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.core.resolution.AccessDeniedResponseAttributes;
import net.brightroom.endpointgate.spring.core.resolution.ProblemDetailBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class AccessDeniedHandlerFilterResolutionViaJsonResponse
    implements AccessDeniedHandlerFilterResolution {

  @Override
  public Mono<ServerResponse> resolve(ServerRequest request, EndpointGateAccessDeniedException e) {
    var body = ProblemDetailBuilder.build(request.path(), e);
    var status = AccessDeniedResponseAttributes.resolveStatus(e);
    var builder = ServerResponse.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON);
    String retryAfter = AccessDeniedResponseAttributes.formatRetryAfter(e);
    if (retryAfter != null) {
      builder = builder.header(HttpHeaders.RETRY_AFTER, retryAfter);
    }
    return builder.bodyValue(body);
  }

  AccessDeniedHandlerFilterResolutionViaJsonResponse() {}
}
