package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EndpointGateMethodLevelViewController {

  @GetMapping("/stable")
  String stableEndpoint() {
    return "stable";
  }

  @EndpointGate("experimental-stage-endpoint")
  @GetMapping("/experimental-stage")
  String experimentalStageEndpoint() {
    return "experimental-stage";
  }

  @EndpointGate("development-stage-endpoint")
  @GetMapping("/development-stage")
  String developmentStageEndpoint() {
    return "development-stage";
  }

  public EndpointGateMethodLevelViewController() {}
}
