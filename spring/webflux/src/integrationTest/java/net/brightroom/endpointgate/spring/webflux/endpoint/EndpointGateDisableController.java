package net.brightroom.endpointgate.spring.webflux.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@EndpointGate("disable-class-level-feature")
public class EndpointGateDisableController {

  @GetMapping("/test/disable")
  Mono<String> testDisable() {
    return Mono.just("Not Allowed");
  }

  @EndpointGate("experimental-stage-endpoint")
  @GetMapping("/test/method-override")
  Mono<String> testMethodOverride() {
    return Mono.just("Method Override Allowed");
  }

  public EndpointGateDisableController() {}
}
