package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EndpointGate("disable-class-level-feature")
public class EndpointGateDisableController {

  @GetMapping("/test/disable")
  String testDisable() {
    return "Not Allowed";
  }
}
