package net.brightroom.endpointgate.spring.webflux.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@EndpointGate("rollout-feature")
public class EndpointGateClassRolloutController {

  @GetMapping("/test/class-rollout")
  public Mono<String> testClassRollout() {
    return Mono.just("Allowed");
  }

  public EndpointGateClassRolloutController() {}
}
