package net.brightroom.example.errorhandling;

import java.net.URI;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * C-2: Custom @ControllerAdvice that redirects to /coming-soon.
 *
 * <p>WebFlux does not support RedirectView directly; use ResponseEntity with Location header.
 */
@ControllerAdvice
@Order(0)
@Profile("redirect")
public class RedirectEndpointGateExceptionHandler {

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  public ResponseEntity<Void> handleEndpointGateAccessDenied(EndpointGateAccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/coming-soon")).build();
  }
}
