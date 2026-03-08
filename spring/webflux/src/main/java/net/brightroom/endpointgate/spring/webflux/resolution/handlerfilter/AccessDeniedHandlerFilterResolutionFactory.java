package net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter;

import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.core.properties.ResponseProperties;

/**
 * Factory for creating {@link AccessDeniedHandlerFilterResolution} instances.
 *
 * <p>Selects the appropriate resolution implementation based on the configured {@code
 * endpoint-gate.response.type} property.
 */
public class AccessDeniedHandlerFilterResolutionFactory {

  /**
   * Creates an {@link AccessDeniedHandlerFilterResolution} based on the response type configured in
   * the given {@link EndpointGateProperties}.
   *
   * @param endpointGateProperties the endpoint gate configuration properties
   * @return the resolution implementation matching the configured response type
   */
  public AccessDeniedHandlerFilterResolution create(EndpointGateProperties endpointGateProperties) {
    ResponseProperties responseProperties = endpointGateProperties.response();

    return switch (responseProperties.type()) {
      case PLAIN_TEXT -> new AccessDeniedHandlerFilterResolutionViaPlainTextResponse();
      case HTML -> new AccessDeniedHandlerFilterResolutionViaHtmlResponse();
      case JSON -> new AccessDeniedHandlerFilterResolutionViaJsonResponse();
    };
  }

  /** Creates a new {@code AccessDeniedHandlerFilterResolutionFactory}. */
  public AccessDeniedHandlerFilterResolutionFactory() {}
}
