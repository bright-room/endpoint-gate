package net.brightroom.example.rollout;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigRolloutController {

  @GetMapping("/api/config-rollout")
  @EndpointGate("config-rollout")
  public String configRollout() {
    return "config-rollout: you are in the rollout group!";
  }
}
