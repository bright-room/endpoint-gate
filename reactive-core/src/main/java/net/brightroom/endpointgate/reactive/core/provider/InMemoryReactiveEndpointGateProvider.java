package net.brightroom.endpointgate.reactive.core.provider;

import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * An implementation of {@link ReactiveEndpointGateProvider} that stores gate configurations in
 * memory using a {@link Map}.
 *
 * <p>This class provides a simple, immutable in-memory mechanism to check whether specific gates
 * are enabled or disabled reactively.
 *
 * <p><b>Fail-closed behavior (default):</b> If {@code gateId} is not present in the gate map, this
 * provider returns the value of {@code defaultEnabled} (defaults to {@code false}).
 */
public class InMemoryReactiveEndpointGateProvider implements ReactiveEndpointGateProvider {

  private final Map<String, Boolean> gates;
  private final boolean defaultEnabled;

  /**
   * Returns whether the specified gate is enabled.
   *
   * @param gateId the identifier of the gate to check
   * @return a {@link Mono} emitting {@code true} if the gate is enabled, {@code false} if disabled
   *     or not configured
   */
  @Override
  public Mono<Boolean> isGateEnabled(String gateId) {
    return Mono.just(gates.getOrDefault(gateId, defaultEnabled));
  }

  /**
   * Constructs an instance with the provided gates and default enabled status.
   *
   * @param gates a map containing gate identifiers as keys and their activation status as values;
   *     copied defensively on construction
   * @param defaultEnabled the default enabled status for gates not present in the map
   */
  public InMemoryReactiveEndpointGateProvider(Map<String, Boolean> gates, boolean defaultEnabled) {
    this.gates = Map.copyOf(gates);
    this.defaultEnabled = defaultEnabled;
  }
}
