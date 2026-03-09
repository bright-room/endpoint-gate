package net.brightroom.example.customrolloutstrategy;

import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.spring.webflux.context.ReactiveEndpointGateContextResolver;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("region")
public class RegionReactiveContextResolver implements ReactiveEndpointGateContextResolver {

  @Override
  public Mono<EndpointGateContext> resolve(ServerHttpRequest request) {
    String region = request.getHeaders().getFirst("X-Region");
    if (region == null) {
      return Mono.empty();
    }
    if (region.isBlank()) {
      return Mono.empty();
    }
    return Mono.just(new EndpointGateContext(region));
  }
}
