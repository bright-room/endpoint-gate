package net.brightroom.example.rollout;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StickyRolloutController {

  @EndpointGate("sticky-rollout")
  @GetMapping("/api/sticky-rollout")
  public Mono<String> stickyRollout() {
    return Mono.just("sticky-rollout: you are in the rollout group!");
  }
}
