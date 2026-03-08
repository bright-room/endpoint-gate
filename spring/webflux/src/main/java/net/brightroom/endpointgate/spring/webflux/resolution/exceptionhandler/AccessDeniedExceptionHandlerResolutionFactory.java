package net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler;

import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.core.properties.ResponseProperties;

/**
 * Factory for creating {@link AccessDeniedExceptionHandlerResolution} instances.
 *
 * <p>Selects the appropriate resolution implementation based on the configured {@code
 * endpoint-gate.response.type} property.
 */
public class AccessDeniedExceptionHandlerResolutionFactory {

  /**
   * Creates an {@link AccessDeniedExceptionHandlerResolution} based on the response type configured
   * in the given {@link EndpointGateProperties}.
   *
   * @param endpointGateProperties the endpoint gate configuration properties
   * @return the resolution implementation matching the configured response type
   */
  public AccessDeniedExceptionHandlerResolution create(
      EndpointGateProperties endpointGateProperties) {
    ResponseProperties responseProperties = endpointGateProperties.response();

    return switch (responseProperties.type()) {
      case PLAIN_TEXT -> new AccessDeniedExceptionHandlerResolutionViaPlainTextResponse();
      case HTML -> new AccessDeniedExceptionHandlerResolutionViaHtmlResponse();
      case JSON -> new AccessDeniedExceptionHandlerResolutionViaJsonResponse();
    };
  }

  /** Creates a new {@code AccessDeniedExceptionHandlerResolutionFactory}. */
  public AccessDeniedExceptionHandlerResolutionFactory() {}
}
