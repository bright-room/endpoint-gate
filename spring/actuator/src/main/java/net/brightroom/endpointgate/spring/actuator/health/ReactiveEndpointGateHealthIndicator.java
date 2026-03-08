package net.brightroom.endpointgate.spring.actuator.health;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import org.springframework.boot.health.contributor.AbstractReactiveHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive {@link org.springframework.boot.health.contributor.ReactiveHealthIndicator
 * ReactiveHealthIndicator} for the endpoint gate provider.
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
 * <p>When the provider implements {@link MutableReactiveEndpointGateProvider}, gate information is
 * retrieved via {@link MutableReactiveEndpointGateProvider#getGates()}. Otherwise, the configured
 * gate IDs from {@link EndpointGateProperties} are probed individually via {@link
 * ReactiveEndpointGateProvider#isGateEnabled(String)}.
 *
 * <p>When a timeout is configured via {@link EndpointGateHealthProperties#timeout()}, the health
 * check will report {@code DOWN} if the provider does not respond within that duration.
 *
 * <p>Additional details can be contributed by registering {@link ReactiveHealthDetailsContributor}
 * beans.
 */
public class ReactiveEndpointGateHealthIndicator extends AbstractReactiveHealthIndicator {

  private final ReactiveEndpointGateProvider provider;
  private final EndpointGateProperties properties;
  private final Duration timeout;
  private final List<ReactiveHealthDetailsContributor> contributors;

  /**
   * Creates a new {@link ReactiveEndpointGateHealthIndicator}.
   *
   * @param provider the reactive endpoint gate provider to check
   * @param properties the endpoint gate configuration properties
   */
  public ReactiveEndpointGateHealthIndicator(
      ReactiveEndpointGateProvider provider, EndpointGateProperties properties) {
    this(provider, properties, null, List.of());
  }

  /**
   * Creates a new {@link ReactiveEndpointGateHealthIndicator} with timeout and custom detail
   * contributors.
   *
   * @param provider the reactive endpoint gate provider to check
   * @param properties the endpoint gate configuration properties
   * @param timeout the maximum time to wait for the provider, or {@code null} for no timeout
   * @param contributors the list of contributors that add custom details to the health response
   */
  public ReactiveEndpointGateHealthIndicator(
      ReactiveEndpointGateProvider provider,
      EndpointGateProperties properties,
      Duration timeout,
      List<ReactiveHealthDetailsContributor> contributors) {
    super("Endpoint gate health check failed");
    this.provider = provider;
    this.properties = properties;
    this.timeout = timeout;
    this.contributors = contributors;
  }

  @Override
  protected Mono<Health> doHealthCheck(Health.Builder builder) {
    Mono<Map<String, Boolean>> gatesMono;
    if (provider instanceof MutableReactiveEndpointGateProvider mutableProvider) {
      gatesMono = mutableProvider.getGates();
    } else {
      gatesMono =
          Flux.fromIterable(properties.gateIds().keySet())
              .flatMap(id -> provider.isGateEnabled(id).map(enabled -> Map.entry(id, enabled)))
              .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    if (timeout != null) {
      gatesMono = gatesMono.timeout(timeout);
    }

    return gatesMono.flatMap(
        gates -> {
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

          return Flux.fromIterable(contributors)
              .flatMap(ReactiveHealthDetailsContributor::contributeDetails)
              .doOnNext(details -> details.forEach(builder::withDetail))
              .then(Mono.fromCallable(builder::build));
        });
  }
}
