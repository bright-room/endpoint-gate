package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointGateRolloutController {

  @GetMapping("/test/rollout")
  @EndpointGate("rollout-gate")
  public String testRollout() {
    return "Allowed";
  }
}
