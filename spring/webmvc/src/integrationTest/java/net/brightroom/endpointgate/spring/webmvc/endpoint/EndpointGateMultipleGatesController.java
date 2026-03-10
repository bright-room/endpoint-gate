package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointGateMultipleGatesController {

  @EndpointGate({"gate-a", "gate-b"})
  @GetMapping("/multiple-gates/all-enabled")
  String allEnabled() {
    return "Allowed";
  }

  @EndpointGate({"gate-a", "gate-disabled"})
  @GetMapping("/multiple-gates/one-disabled")
  String oneDisabled() {
    return "Not Allowed";
  }

  @EndpointGate({"gate-disabled", "gate-a"})
  @GetMapping("/multiple-gates/first-disabled")
  String firstDisabled() {
    return "Not Allowed";
  }
}
