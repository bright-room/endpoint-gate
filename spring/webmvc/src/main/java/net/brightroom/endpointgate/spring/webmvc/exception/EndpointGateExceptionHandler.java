package net.brightroom.endpointgate.spring.webmvc.exception;

import jakarta.servlet.http.HttpServletRequest;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.webmvc.resolution.AccessDeniedInterceptResolution;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
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

  private final AccessDeniedInterceptResolution accessDeniedInterceptResolution;

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  ResponseEntity<?> handleEndpointGateAccessDenied(
      HttpServletRequest request, EndpointGateAccessDeniedException e) {
    return accessDeniedInterceptResolution.resolution(request, e);
  }

  /**
   * Creates a new {@link EndpointGateExceptionHandler} with the given resolution strategy.
   *
   * @param accessDeniedInterceptResolution the resolution to use when access is denied; must not be
   *     null
   */
  public EndpointGateExceptionHandler(
      AccessDeniedInterceptResolution accessDeniedInterceptResolution) {
    this.accessDeniedInterceptResolution = accessDeniedInterceptResolution;
  }
}
