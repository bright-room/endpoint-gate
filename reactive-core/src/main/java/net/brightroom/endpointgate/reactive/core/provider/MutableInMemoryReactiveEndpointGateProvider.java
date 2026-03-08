package net.brightroom.endpointgate.reactive.core.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;

/**
 * A thread-safe, in-memory implementation of {@link MutableReactiveEndpointGateProvider}.
 *
 * <p>Gates are stored in a {@link ConcurrentHashMap}, allowing concurrent reads and writes without
 * external synchronization. The fail-closed/fail-open policy is controlled by {@code
 * defaultEnabled}: when {@code false} (the default), unknown gates are treated as disabled.
 */
public class MutableInMemoryReactiveEndpointGateProvider
    implements MutableReactiveEndpointGateProvider {

  private final ConcurrentHashMap<String, Boolean> gates;
  private final boolean defaultEnabled;

  /**
   * {@inheritDoc}
   *
   * <p>Returns the value of {@code defaultEnabled} for gates not present in the map.
   */
  @Override
  public Mono<Boolean> isGateEnabled(String gateId) {
    return Mono.just(gates.getOrDefault(gateId, defaultEnabled));
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Map<String, Boolean>> getGates() {
    return Mono.just(Map.copyOf(gates));
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Void> setGateEnabled(String gateId, boolean enabled) {
    gates.put(gateId, enabled);
    return Mono.empty();
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Boolean> removeGate(String gateId) {
    return Mono.just(gates.remove(gateId) != null);
  }

  /**
   * Constructs a {@code MutableInMemoryReactiveEndpointGateProvider} with the given initial gates
   * and default enabled state.
   *
   * @param gates the initial gate map; copied defensively on construction
   * @param defaultEnabled the fallback value for gates not present in the map
   */
  public MutableInMemoryReactiveEndpointGateProvider(
      Map<String, Boolean> gates, boolean defaultEnabled) {
    this.gates = new ConcurrentHashMap<>(gates);
    this.defaultEnabled = defaultEnabled;
  }
}
