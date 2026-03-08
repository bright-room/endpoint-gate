package net.brightroom.example.health;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

  @GetMapping("/api/active")
  @EndpointGate("active-feature")
  public String active() {
    return "active-feature is enabled!";
  }

  @GetMapping("/api/inactive")
  @EndpointGate("inactive-feature")
  public String inactive() {
    return "inactive-feature is enabled!";
  }

  @GetMapping("/api/rollout")
  @EndpointGate("rollout-feature")
  public String rollout() {
    return "rollout-feature is enabled!";
  }
}
