package net.brightroom.endpointgate.spring.webflux.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class EndpointGateMultipleGatesController {

  @EndpointGate({"gate-a", "gate-b"})
  @GetMapping("/multiple-gates/all-enabled")
  Mono<String> allEnabled() {
    return Mono.just("Allowed");
  }

  @EndpointGate({"gate-a", "gate-disabled"})
  @GetMapping("/multiple-gates/one-disabled")
  Mono<String> oneDisabled() {
    return Mono.just("Not Allowed");
  }

  @EndpointGate({"gate-disabled", "gate-a"})
  @GetMapping("/multiple-gates/first-disabled")
  Mono<String> firstDisabled() {
    return Mono.just("Not Allowed");
  }

  public EndpointGateMultipleGatesController() {}
}
