package net.brightroom.endpointgate.reactive.core.provider;

import reactor.core.publisher.Mono;

/**
 * Provides a reactive mechanism to check the status of endpoint gates within an application.
 *
 * <p>The {@code ReactiveEndpointGateProvider} interface allows implementations to define how gates
 * are stored and accessed in a non-blocking manner, enabling a consistent method for determining
 * whether a specific gate is enabled or disabled at runtime.
 *
 * <p><b>Undefined gate policy:</b> Implementations must decide what to return when {@code gateId}
 * is not known to the provider. The built-in {@link InMemoryReactiveEndpointGateProvider} uses a
 * <em>fail-closed</em> policy by default (returns {@code false} for unknown gates), which can be
 * changed to fail-open via {@code endpoint-gate.default-enabled: true}. Custom implementations
 * should document their own policy clearly.
 */
public interface ReactiveEndpointGateProvider {

  /**
   * Determines whether a specific gate is enabled.
   *
   * <p>The return value for a gate identifier that is not managed by this provider is
   * implementation-defined. See the implementing class for its undefined-gate policy.
   *
   * @param gateId the identifier of the gate whose status is to be verified
   * @return a {@link Mono} emitting {@code true} if the gate is enabled, {@code false} otherwise
   */
  Mono<Boolean> isGateEnabled(String gateId);
}
