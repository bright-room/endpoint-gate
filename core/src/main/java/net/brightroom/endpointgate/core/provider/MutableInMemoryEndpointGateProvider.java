package net.brightroom.endpointgate.core.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe, in-memory implementation of {@link MutableEndpointGateProvider}.
 *
 * <p>Gates are stored in a {@link ConcurrentHashMap}, allowing concurrent reads and writes without
 * external synchronization.
 */
public class MutableInMemoryEndpointGateProvider implements MutableEndpointGateProvider {

  private final ConcurrentHashMap<String, Boolean> gates;
  private final boolean defaultEnabled;

  /** {@inheritDoc} */
  @Override
  public boolean isGateEnabled(String gateId) {
    return gates.getOrDefault(gateId, defaultEnabled);
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, Boolean> getGates() {
    return Map.copyOf(gates);
  }

  /** {@inheritDoc} */
  @Override
  public void setGateEnabled(String gateId, boolean enabled) {
    gates.put(gateId, enabled);
  }

  /** {@inheritDoc} */
  @Override
  public boolean removeGate(String gateId) {
    return gates.remove(gateId) != null;
  }

  /**
   * Constructs a {@code MutableInMemoryEndpointGateProvider} with the given initial gates and
   * default enabled state.
   *
   * @param gates the initial gate map; copied defensively on construction
   * @param defaultEnabled the fallback value for gates not present in the map
   */
  public MutableInMemoryEndpointGateProvider(Map<String, Boolean> gates, boolean defaultEnabled) {
    this.gates = new ConcurrentHashMap<>(gates);
    this.defaultEnabled = defaultEnabled;
  }
}
