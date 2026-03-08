package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EndpointGate("enable-class-level-feature")
public class EndpointGateEnableController {

  @GetMapping("/test/enabled")
  String testEnabled() {
    return "Allowed";
  }
}
