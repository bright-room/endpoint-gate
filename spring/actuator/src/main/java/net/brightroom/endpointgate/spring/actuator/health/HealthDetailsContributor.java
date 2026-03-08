package net.brightroom.endpointgate.spring.actuator.health;

import java.util.Map;

/**
 * Strategy interface for contributing additional details to an endpoint gate health response.
 *
 * <p>Implementations can be registered as Spring beans to extend the default health details
 * provided by {@link EndpointGateHealthIndicator}.
 */
public interface HealthDetailsContributor {

  /**
   * Returns a map of detail entries to add to the health response.
   *
   * @return a map of key-value pairs to include in the health details
   */
  Map<String, Object> contributeDetails();
}
