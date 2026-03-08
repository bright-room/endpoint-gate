package net.brightroom.endpointgate.core.provider;

import java.util.Map;

/**
 * An extension of {@link EndpointGateProvider} that supports dynamic mutation of gates at runtime.
 *
 * <p>Implementations must be thread-safe, as gates may be read and updated concurrently.
 *
 * <p>This interface serves as an SPI for external storage backends (e.g., Redis, databases) that
 * need to expose mutable gate state without depending on the {@code actuator} module.
 */
public interface MutableEndpointGateProvider extends EndpointGateProvider {

  /**
   * Returns a snapshot of all currently configured gates and their enabled states.
   *
   * <p>The returned map must be an immutable copy; mutations to the returned map must not affect
   * the provider's internal state.
   *
   * @return an immutable map of gate identifiers to their enabled states
   */
  Map<String, Boolean> getGates();

  /**
   * Updates the enabled state of the specified gate.
   *
   * <p>If the gate does not exist, it must be created with the given state.
   *
   * @param gateId the identifier of the gate to update
   * @param enabled {@code true} to enable the gate, {@code false} to disable it
   */
  void setGateEnabled(String gateId, boolean enabled);

  /**
   * Removes the specified gate from this provider.
   *
   * <p>After removal, {@link #isGateEnabled(String)} for this gate will return the default enabled
   * value. If the gate does not exist, this method is a no-op and returns {@code false}.
   *
   * @param gateId the identifier of the gate to remove
   * @return {@code true} if the gate existed and was removed, {@code false} if it did not exist
   */
  boolean removeGate(String gateId);
}
