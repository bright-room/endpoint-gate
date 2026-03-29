package net.brightroom.example.metrics;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

  @GetMapping("/api/enabled")
  @EndpointGate("enabled-feature")
  public Mono<String> enabled() {
    return Mono.just("enabled-feature is accessible!");
  }

  @GetMapping("/api/disabled")
  @EndpointGate("disabled-feature")
  public Mono<String> disabled() {
    return Mono.just("disabled-feature is accessible!");
  }

  @GetMapping("/api/rollout")
  @EndpointGate("rollout-feature")
  public Mono<String> rollout() {
    return Mono.just("rollout-feature is accessible!");
  }
}
