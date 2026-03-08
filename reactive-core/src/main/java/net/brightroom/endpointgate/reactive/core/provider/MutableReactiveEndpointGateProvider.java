package net.brightroom.endpointgate.reactive.core.provider;

import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * A reactive extension of {@link ReactiveEndpointGateProvider} that supports dynamic mutation of
 * gates at runtime.
 *
 * <p>Implementations must be thread-safe, as gates may be read and updated concurrently.
 *
 * <p>This interface serves as an SPI for external storage backends (e.g., Redis, databases) that
 * need to expose mutable gate state in a reactive manner without depending on the {@code actuator}
 * module.
 */
public interface MutableReactiveEndpointGateProvider extends ReactiveEndpointGateProvider {

  /**
   * Returns a snapshot of all currently configured gates and their enabled states.
   *
   * <p>The returned map must be an immutable copy; mutations to the returned map must not affect
   * the provider's internal state.
   *
   * @return a {@link Mono} emitting an immutable map of gate identifiers to their enabled states
   */
  Mono<Map<String, Boolean>> getGates();

  /**
   * Updates the enabled state of the specified gate.
   *
   * <p>If the gate does not exist, it must be created with the given state.
   *
   * <p><b>Note:</b> This method does not publish {@code EndpointGateChangedEvent}. Event publishing
   * is handled by the actuator endpoint. If you call this method directly and need event
   * notification, publish the event manually via {@code ApplicationEventPublisher}.
   *
   * @param gateId the identifier of the gate to update
   * @param enabled {@code true} to enable the gate, {@code false} to disable it
   * @return a {@link Mono} that completes when the update is applied
   */
  Mono<Void> setGateEnabled(String gateId, boolean enabled);

  /**
   * Removes the specified gate from this provider.
   *
   * <p>After removal, {@link #isGateEnabled(String)} for this gate will return the default enabled
   * value. If the gate does not exist, this method is a no-op and emits {@code false}.
   *
   * <p><b>Note:</b> This method does not publish {@code EndpointGateRemovedEvent}. Event publishing
   * is handled by the actuator endpoint. If you call this method directly and need event
   * notification, publish the event manually via {@code ApplicationEventPublisher}.
   *
   * @param gateId the identifier of the gate to remove
   * @return a {@link Mono} emitting {@code true} if the gate existed and was removed, {@code false}
   *     if it did not exist
   */
  Mono<Boolean> removeGate(String gateId);
}
