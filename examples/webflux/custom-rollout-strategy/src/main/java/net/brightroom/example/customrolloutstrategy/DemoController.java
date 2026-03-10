package net.brightroom.example.customrolloutstrategy;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

  @GetMapping("/api/feature")
  @EndpointGate("gradual-feature")
  public Mono<String> feature() {
    return Mono.just("You are in the rollout group for gradual-feature!");
  }
}
