package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EndpointGate("enable-class-level-feature")
public class EndpointGateEnableViewController {

  @GetMapping("/view/test/enabled")
  String testEnabled() {
    return "enabled";
  }
}
