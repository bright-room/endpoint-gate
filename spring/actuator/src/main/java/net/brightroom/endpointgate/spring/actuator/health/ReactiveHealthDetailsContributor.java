package net.brightroom.endpointgate.spring.actuator.health;

import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Reactive strategy interface for contributing additional details to an endpoint gate health
 * response.
 *
 * <p>Implementations can be registered as Spring beans to extend the default health details
 * provided by {@link ReactiveEndpointGateHealthIndicator}.
 */
public interface ReactiveHealthDetailsContributor {

  /**
   * Returns a {@link Mono} emitting a map of detail entries to add to the health response.
   *
   * @return a {@link Mono} emitting a map of key-value pairs to include in the health details
   */
  Mono<Map<String, Object>> contributeDetails();
}
