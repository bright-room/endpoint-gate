package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@ControllerAdvice
@Order(0)
@Profile("xml-error")
public class XmlEndpointGateExceptionHandler {

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  public ResponseEntity<EndpointGateError> handleEndpointGateAccessDenied(
      EndpointGateAccessDeniedException ex) {
    var error =
        new EndpointGateError(
            "coming_soon", ex.gateId(), "This feature is not yet available. Stay tuned!");
    return ResponseEntity.status(403).contentType(MediaType.APPLICATION_XML).body(error);
  }

  @JacksonXmlRootElement(localName = "endpoint-gate-error")
  public record EndpointGateError(String error, String gate, String message) {}
}
