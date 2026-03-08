package net.brightroom.endpointgate.spring.webflux.exception;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler.AccessDeniedExceptionHandlerResolution;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Default handler for {@link EndpointGateAccessDeniedException}.
 *
 * <p>This handler has the lowest precedence ({@code @Order(Ordered.LOWEST_PRECEDENCE)}), so any
 * user-defined {@code @ControllerAdvice} handling the same exception will take priority.
 *
 * <p>If you want to guarantee that your {@code @ControllerAdvice} takes priority, annotate it with
 * an order value lower than {@code Ordered.LOWEST_PRECEDENCE} (e.g.,
 * {@code @Order(Ordered.LOWEST_PRECEDENCE - 1)} or simply {@code @Order(0)}).
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class EndpointGateExceptionHandler {

  private final AccessDeniedExceptionHandlerResolution accessDeniedExceptionHandlerResolution;

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  ResponseEntity<?> handleEndpointGateAccessDenied(
      ServerHttpRequest request, EndpointGateAccessDeniedException e) {
    return accessDeniedExceptionHandlerResolution.resolution(request, e);
  }

  /**
   * Creates a new {@code EndpointGateExceptionHandler}.
   *
   * @param accessDeniedExceptionHandlerResolution the resolution used to build the denied response
   */
  public EndpointGateExceptionHandler(
      AccessDeniedExceptionHandlerResolution accessDeniedExceptionHandlerResolution) {
    this.accessDeniedExceptionHandlerResolution = accessDeniedExceptionHandlerResolution;
  }
}
