package net.brightroom.endpointgate.core.provider;

/**
 * Provides a mechanism to check the status of endpoint gates within an application.
 *
 * <p>The {@code EndpointGateProvider} interface allows implementations to define how gates are
 * stored and accessed, enabling a consistent method for determining whether a specific gate is
 * enabled or disabled at runtime.
 *
 * <p><b>Undefined gate policy:</b> Implementations must decide what to return when {@code gateId}
 * is not known to the provider. The built-in {@link InMemoryEndpointGateProvider} uses a
 * <em>fail-closed</em> policy by default (returns {@code false} for unknown gates), which can be
 * changed to fail-open via {@code endpoint-gate.default-enabled: true}. Custom implementations
 * should document their own policy clearly.
 */
public interface EndpointGateProvider {

  /**
   * Determines whether a specific gate is enabled.
   *
   * <p>The return value for a gate identifier that is not managed by this provider is
   * implementation-defined. See the implementing class for its undefined-gate policy.
   *
   * @param gateId the identifier of the gate whose status is to be verified
   * @return {@code true} if the gate is enabled, {@code false} otherwise
   */
  boolean isGateEnabled(String gateId);
}
