package net.brightroom.endpointgate.spring.actuator.endpoint;

import org.jspecify.annotations.Nullable;

/**
 * Response body for the {@code /actuator/endpoint-gates/{gateId}} endpoint.
 *
 * <p>Contains the state of a single endpoint gate at the time of the request.
 *
 * @param gateId the identifier of the endpoint gate
 * @param enabled the current enabled state of the endpoint gate
 * @param rollout the current rollout percentage (0–100) of the endpoint gate
 * @param condition the current condition expression, or {@code null} if no condition is configured
 * @param schedule the schedule configuration for this gate, or {@code null} if no schedule is
 *     configured
 */
public record EndpointGateEndpointResponse(
    String gateId,
    boolean enabled,
    int rollout,
    @Nullable String condition,
    @Nullable ScheduleEndpointResponse schedule) {

  /**
   * Creates a response without condition or schedule information.
   *
   * @param gateId the identifier of the endpoint gate
   * @param enabled the current enabled state of the endpoint gate
   * @param rollout the current rollout percentage (0–100) of the endpoint gate
   */
  public EndpointGateEndpointResponse(String gateId, boolean enabled, int rollout) {
    this(gateId, enabled, rollout, null, null);
  }

  /**
   * Creates a response without schedule information.
   *
   * @param gateId the identifier of the endpoint gate
   * @param enabled the current enabled state of the endpoint gate
   * @param rollout the current rollout percentage (0–100) of the endpoint gate
   * @param condition the current condition expression, or {@code null} if no condition is
   *     configured
   */
  public EndpointGateEndpointResponse(
      String gateId, boolean enabled, int rollout, @Nullable String condition) {
    this(gateId, enabled, rollout, condition, null);
  }
}
