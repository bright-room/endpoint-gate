package net.brightroom.example.rollout;

import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.spring.webflux.context.ReactiveEndpointGateContextResolver;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("sticky")
public class UserIdReactiveContextResolver implements ReactiveEndpointGateContextResolver {

  @Override
  public Mono<EndpointGateContext> resolve(ServerHttpRequest request) {
    String userId = request.getQueryParams().getFirst("userId");
    if (userId == null || userId.isBlank()) {
      return Mono.empty();
    }
    return Mono.just(new EndpointGateContext(userId));
  }
}
