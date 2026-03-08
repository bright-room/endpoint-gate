package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EndpointGate("rollout-gate")
public class EndpointGateClassRolloutController {

  @GetMapping("/test/class-rollout")
  public String testClassRollout() {
    return "Allowed";
  }
}
