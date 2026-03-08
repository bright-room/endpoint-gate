package net.brightroom.endpointgate.core.provider;

import java.util.Map;

/**
 * An implementation of {@link EndpointGateProvider} that stores gate configurations in memory using
 * a {@link Map}.
 *
 * <p>This class provides a simple mechanism to check whether specific gates are enabled or disabled
 * based on an in-memory map.
 */
public class InMemoryEndpointGateProvider implements EndpointGateProvider {

  private final Map<String, Boolean> gates;
  private final boolean defaultEnabled;

  /**
   * Returns whether the specified gate is enabled.
   *
   * <p><b>Fail-closed behavior (default):</b> If {@code gateId} is not present in the gate map,
   * this method returns the value of {@code defaultEnabled} (defaults to {@code false}).
   *
   * @param gateId the identifier of the gate to check
   * @return {@code true} if the gate is explicitly enabled, {@code false} if explicitly disabled or
   *     not configured (with default fail-closed behavior)
   */
  @Override
  public boolean isGateEnabled(String gateId) {
    return gates.getOrDefault(gateId, defaultEnabled);
  }

  /**
   * Constructs an instance with the provided gates and default enabled status.
   *
   * @param gates a map containing gate identifiers as keys and their activation status as values;
   *     copied defensively on construction
   * @param defaultEnabled the default enabled status for gates not present in the map
   */
  public InMemoryEndpointGateProvider(Map<String, Boolean> gates, boolean defaultEnabled) {
    this.gates = Map.copyOf(gates);
    this.defaultEnabled = defaultEnabled;
  }
}
