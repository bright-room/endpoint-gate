package net.brightroom.example.customrolloutstrategy;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

  @GetMapping("/api/gradual")
  @EndpointGate("gradual-feature")
  public String gradual() {
    return "You are in the rollout group for gradual-feature!";
  }
}
