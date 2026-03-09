package net.brightroom.example.errorhandling;

import java.util.Map;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * C-2: Custom @ControllerAdvice exception handler for Annotated Controller path.
 *
 * <p>Uses @Order(0) to take precedence over the default handler (LOWEST_PRECEDENCE).
 */
@ControllerAdvice
@Order(0)
@Profile("json-error")
public class CustomEndpointGateExceptionHandler {

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleEndpointGateAccessDenied(
      EndpointGateAccessDeniedException ex) {
    return ResponseEntity.status(403)
        .body(
            Map.of(
                "error", "coming_soon",
                "gate", ex.gateId(),
                "message", "This feature is not yet available. Stay tuned!"));
  }
}
