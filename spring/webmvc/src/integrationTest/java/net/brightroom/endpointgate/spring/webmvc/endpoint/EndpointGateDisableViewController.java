package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EndpointGate("disable-class-level-feature")
public class EndpointGateDisableViewController {

  @GetMapping("/view/test/disable")
  String testDisable() {
    return "disable";
  }
}
