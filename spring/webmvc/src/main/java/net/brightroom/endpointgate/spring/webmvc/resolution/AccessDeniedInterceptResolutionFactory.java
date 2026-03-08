package net.brightroom.endpointgate.spring.webmvc.resolution;

import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.core.properties.ResponseProperties;

/**
 * Factory for creating {@link AccessDeniedInterceptResolution} instances based on the configured
 * response type.
 *
 * <p>Selects the appropriate resolution implementation from {@link
 * net.brightroom.endpointgate.spring.core.properties.ResponseType ResponseType} configured via
 * {@code endpoint-gate.response.type}.
 */
public class AccessDeniedInterceptResolutionFactory {

  /**
   * Creates an {@link AccessDeniedInterceptResolution} appropriate for the response type configured
   * in the given properties.
   *
   * @param endpointGateProperties the endpoint gate configuration properties; must not be null
   * @return the resolution implementation matching the configured response type
   */
  public AccessDeniedInterceptResolution create(EndpointGateProperties endpointGateProperties) {
    ResponseProperties responseProperties = endpointGateProperties.response();

    return switch (responseProperties.type()) {
      case PLAIN_TEXT -> new AccessDeniedInterceptResolutionViaPlainTextResponse();
      case HTML -> new AccessDeniedInterceptResolutionViaHtmlResponse();
      case JSON -> new AccessDeniedInterceptResolutionViaJsonResponse();
    };
  }

  /** Creates a new {@link AccessDeniedInterceptResolutionFactory}. */
  public AccessDeniedInterceptResolutionFactory() {}
}
