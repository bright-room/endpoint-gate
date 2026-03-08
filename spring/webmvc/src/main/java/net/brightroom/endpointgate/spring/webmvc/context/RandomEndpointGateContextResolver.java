package net.brightroom.endpointgate.spring.webmvc.context;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import net.brightroom.endpointgate.core.context.EndpointGateContext;

/**
 * Default {@link EndpointGateContextResolver} that generates a random UUID per request.
 *
 * <p>This provides non-sticky (per-request probabilistic) rollout behavior. Each request
 * independently has a rollout-percentage chance of being included.
 *
 * <p>This is the default implementation registered by auto-configuration. Users interact with
 * {@link EndpointGateContextResolver} only.
 */
public class RandomEndpointGateContextResolver implements EndpointGateContextResolver {

  /** Creates a new {@link RandomEndpointGateContextResolver}. */
  public RandomEndpointGateContextResolver() {}

  @Override
  public Optional<EndpointGateContext> resolve(HttpServletRequest request) {
    return Optional.of(new EndpointGateContext(UUID.randomUUID().toString()));
  }
}
