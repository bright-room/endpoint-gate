package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointGateMethodLevelController {

  @GetMapping("/stable-endpoint")
  String stableEndpoint() {
    return "No Annotation";
  }

  @EndpointGate("experimental-stage-endpoint")
  @GetMapping("/experimental-stage-endpoint")
  String experimentalStageEndpoint() {
    return "Allowed";
  }

  @EndpointGate("development-stage-endpoint")
  @GetMapping("/development-stage-endpoint")
  String developmentStageEndpoint() {
    return "Not Allowed";
  }

  public EndpointGateMethodLevelController() {}
}
