package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
@Order(0)
@Profile("redirect")
public class RedirectEndpointGateExceptionHandler {

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  public RedirectView handleEndpointGateAccessDenied(EndpointGateAccessDeniedException ex) {
    return new RedirectView("/coming-soon");
  }
}
