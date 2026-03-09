package net.brightroom.example.rollout;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AnnotationRolloutController {

  @EndpointGate("annotation-rollout")
  @GetMapping("/api/annotation-rollout")
  public Mono<String> rollout() {
    return Mono.just("annotation-rollout: you are in the 50% rollout group!");
  }
}
