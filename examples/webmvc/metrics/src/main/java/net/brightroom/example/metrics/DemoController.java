package net.brightroom.example.metrics;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

  @GetMapping("/api/enabled")
  @EndpointGate("enabled-feature")
  public String enabled() {
    return "enabled-feature is accessible!";
  }

  @GetMapping("/api/disabled")
  @EndpointGate("disabled-feature")
  public String disabled() {
    return "disabled-feature is accessible!";
  }

  @GetMapping("/api/rollout")
  @EndpointGate("rollout-feature")
  public String rollout() {
    return "rollout-feature is accessible!";
  }
}
