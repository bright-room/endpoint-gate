package net.brightroom.endpointgate.spring.webflux.context;

import java.util.UUID;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

/**
 * Default {@link ReactiveEndpointGateContextResolver} that generates a random UUID per request.
 *
 * <p>This provides non-sticky (per-request probabilistic) rollout behavior. Each request
 * independently has a rollout-percentage chance of being included.
 *
 * <p>This is the default implementation registered by auto-configuration. Users interact with
 * {@link ReactiveEndpointGateContextResolver} only.
 */
public class RandomReactiveEndpointGateContextResolver
    implements ReactiveEndpointGateContextResolver {

  /** Creates a new {@code RandomReactiveEndpointGateContextResolver}. */
  public RandomReactiveEndpointGateContextResolver() {}

  @Override
  public Mono<EndpointGateContext> resolve(ServerHttpRequest request) {
    return Mono.just(new EndpointGateContext(UUID.randomUUID().toString()));
  }
}
