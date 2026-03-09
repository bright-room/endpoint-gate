package net.brightroom.example.actuator;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

  @GetMapping("/api/demo")
  @EndpointGate("demo-feature")
  public Mono<String> demo() {
    return Mono.just("demo-feature is enabled!");
  }

  @GetMapping("/api/another")
  @EndpointGate("another-feature")
  public Mono<String> another() {
    return Mono.just("another-feature is enabled!");
  }
}
