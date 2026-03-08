package net.brightroom.endpointgate.spring.actuator.health;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import net.brightroom.endpointgate.core.provider.MutableEndpointGateProvider;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;

/**
 * {@link org.springframework.boot.health.contributor.HealthIndicator HealthIndicator} for the
 * endpoint gate provider.
 *
 * <p>Reports {@link org.springframework.boot.health.contributor.Status#UP UP} when the provider
 * responds normally and gate information can be retrieved, and {@link
 * org.springframework.boot.health.contributor.Status#DOWN DOWN} when an exception occurs during the
 * health check or when the provider does not respond within the configured timeout.
 *
 * <p>Health details include:
 *
 * <ul>
 *   <li>{@code provider} — the simple class name of the provider implementation
 *   <li>{@code totalGates} — total number of endpoint gates
 *   <li>{@code enabledGates} — number of enabled gates
 *   <li>{@code disabledGates} — number of disabled gates
 *   <li>{@code defaultEnabled} — the default-enabled policy from configuration
 * </ul>
 *
 * <p>When the provider implements {@link MutableEndpointGateProvider}, gate information is
 * retrieved via {@link MutableEndpointGateProvider#getGates()}. Otherwise, the configured gate IDs
 * from {@link EndpointGateProperties} are probed individually via {@link
 * EndpointGateProvider#isGateEnabled(String)}.
 *
 * <p>When a timeout is configured via {@link EndpointGateHealthProperties#timeout()}, the health
 * check will report {@code DOWN} if the provider does not respond within that duration.
 *
 * <p>Additional details can be contributed by registering {@link HealthDetailsContributor} beans.
 */
public class EndpointGateHealthIndicator extends AbstractHealthIndicator {

  private final EndpointGateProvider provider;
  private final EndpointGateProperties properties;
  private final Duration timeout;
  private final List<HealthDetailsContributor> contributors;

  /**
   * Creates a new {@link EndpointGateHealthIndicator}.
   *
   * @param provider the endpoint gate provider to check
   * @param properties the endpoint gate configuration properties
   */
  public EndpointGateHealthIndicator(
      EndpointGateProvider provider, EndpointGateProperties properties) {
    this(provider, properties, null, List.of());
  }

  /**
   * Creates a new {@link EndpointGateHealthIndicator} with timeout and custom detail contributors.
   *
   * @param provider the endpoint gate provider to check
   * @param properties the endpoint gate configuration properties
   * @param timeout the maximum time to wait for the provider, or {@code null} for no timeout
   * @param contributors the list of contributors that add custom details to the health response
   */
  public EndpointGateHealthIndicator(
      EndpointGateProvider provider,
      EndpointGateProperties properties,
      Duration timeout,
      List<HealthDetailsContributor> contributors) {
    super("Endpoint gate health check failed");
    this.provider = provider;
    this.properties = properties;
    this.timeout = timeout;
    this.contributors = contributors;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    Map<String, Boolean> gates;
    if (timeout != null) {
      gates =
          CompletableFuture.supplyAsync(this::fetchGates)
              .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } else {
      gates = fetchGates();
    }

    long totalCount = gates.size();
    long enabledCount = gates.values().stream().filter(Boolean::booleanValue).count();
    long disabledCount = totalCount - enabledCount;

    builder
        .up()
        .withDetail("provider", provider.getClass().getSimpleName())
        .withDetail("totalGates", totalCount)
        .withDetail("enabledGates", enabledCount)
        .withDetail("disabledGates", disabledCount)
        .withDetail("defaultEnabled", properties.defaultEnabled());

    for (HealthDetailsContributor contributor : contributors) {
      contributor.contributeDetails().forEach(builder::withDetail);
    }
  }

  private Map<String, Boolean> fetchGates() {
    if (provider instanceof MutableEndpointGateProvider mutableProvider) {
      return mutableProvider.getGates();
    }
    var map = new LinkedHashMap<String, Boolean>();
    for (String id : properties.gateIds().keySet()) {
      map.put(id, provider.isGateEnabled(id));
    }
    return map;
  }
}
