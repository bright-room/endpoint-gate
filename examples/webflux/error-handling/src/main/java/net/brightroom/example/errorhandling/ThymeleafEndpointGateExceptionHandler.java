package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.view.Rendering;

/** C-2: Custom @ControllerAdvice that renders a Thymeleaf error template. */
@ControllerAdvice
@Order(0)
@Profile("thymeleaf-error")
public class ThymeleafEndpointGateExceptionHandler {

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  public Rendering handleEndpointGateAccessDenied(EndpointGateAccessDeniedException ex) {
    return Rendering.view("error/feature-unavailable")
        .status(HttpStatus.FORBIDDEN)
        .modelAttribute("gateName", ex.gateId())
        .build();
  }
}
