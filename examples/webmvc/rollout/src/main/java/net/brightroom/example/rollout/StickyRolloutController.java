package net.brightroom.example.rollout;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StickyRolloutController {

  @GetMapping("/api/sticky-rollout")
  @EndpointGate("sticky-rollout")
  public String stickyRollout() {
    return "sticky-rollout: you are in the rollout group!";
  }
}
