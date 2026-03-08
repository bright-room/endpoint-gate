package net.brightroom.endpointgate.spring.webflux.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class EndpointGateMethodLevelController {

  @GetMapping("/stable-endpoint")
  Mono<String> stableEndpoint() {
    return Mono.just("No Annotation");
  }

  @EndpointGate("experimental-stage-endpoint")
  @GetMapping("/experimental-stage-endpoint")
  Mono<String> experimentalStageEndpoint() {
    return Mono.just("Allowed");
  }

  @EndpointGate("development-stage-endpoint")
  @GetMapping("/development-stage-endpoint")
  Mono<String> developmentStageEndpoint() {
    return Mono.just("Not Allowed");
  }

  public EndpointGateMethodLevelController() {}
}
