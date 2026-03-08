package net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * Interface for handling cases where access to an endpoint gate protected resource is denied in a
 * {@link org.springframework.web.bind.annotation.ControllerAdvice} context.
 *
 * <p>Implementations return a {@link ResponseEntity} which is written through Spring's response
 * processing pipeline, enabling content negotiation and standard message converters.
 */
public interface AccessDeniedExceptionHandlerResolution {

  /**
   * Resolves the response when access to an endpoint gate protected resource is denied.
   *
   * @param request the reactive HTTP request that was denied access
   * @param e the EndpointGateAccessDeniedException that triggered the resolution
   * @return a ResponseEntity representing the denial response
   */
  ResponseEntity<?> resolution(ServerHttpRequest request, EndpointGateAccessDeniedException e);
}
