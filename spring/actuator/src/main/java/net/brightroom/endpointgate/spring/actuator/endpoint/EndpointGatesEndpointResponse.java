package net.brightroom.endpointgate.spring.actuator.endpoint;

import java.util.List;

/**
 * Response body for the {@code /actuator/endpoint-gates} endpoint.
 *
 * <p>Contains a snapshot of all endpoint gates and the default-enabled policy at the time of the
 * request.
 *
 * @param gates a list of all endpoint gates and their current enabled states
 * @param defaultEnabled the fallback value returned for gates not present in {@code gates}
 */
public record EndpointGatesEndpointResponse(
    List<EndpointGateEndpointResponse> gates, boolean defaultEnabled) {}
