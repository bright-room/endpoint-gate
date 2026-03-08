package net.brightroom.endpointgate.spring.webflux.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@EndpointGate("enable-class-level-feature")
public class EndpointGateEnableController {

  @GetMapping("/test/enabled")
  Mono<String> testEnabled() {
    return Mono.just("Allowed");
  }

  public EndpointGateEnableController() {}
}
