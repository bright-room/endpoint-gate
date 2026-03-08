package net.brightroom.endpointgate.spring.webmvc.resolution;

import jakarta.servlet.http.HttpServletRequest;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.http.ResponseEntity;

/**
 * Interface for handling cases where access to an endpoint gate protected resource is denied.
 *
 * <p>The {@link AccessDeniedInterceptResolution} interface serves as a strategy to determine how
 * HTTP requests should be resolved when access is restricted due to a disabled endpoint gate.
 * Implementations of this interface are used in conjunction with the {@link
 * net.brightroom.endpointgate.spring.webmvc.interceptor.EndpointGateInterceptor} to provide
 * customized responses for denied access scenarios.
 *
 * <p>Implementations of this interface can define various resolutions such as returning JSON, plain
 * text, or HTML responses, depending on the application's requirements.
 */
public interface AccessDeniedInterceptResolution {

  /**
   * Resolves the response when access to an endpoint gate protected resource is denied.
   *
   * <p>This method is called by {@link
   * net.brightroom.endpointgate.spring.webmvc.exception.EndpointGateExceptionHandler} when {@link
   * EndpointGateAccessDeniedException} is thrown by the interceptor. Implementations return a
   * {@link ResponseEntity} which is written through Spring's response processing pipeline, enabling
   * content negotiation and standard message converters.
   *
   * @param request the HTTP servlet request that was denied access
   * @param e the EndpointGateAccessDeniedException that triggered the resolution
   * @return a ResponseEntity representing the denial response
   */
  ResponseEntity<?> resolution(HttpServletRequest request, EndpointGateAccessDeniedException e);
}
