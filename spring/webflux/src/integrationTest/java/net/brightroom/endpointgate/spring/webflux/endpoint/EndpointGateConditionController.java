package net.brightroom.endpointgate.spring.webflux.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class EndpointGateConditionController {

  @EndpointGate("header-condition-feature")
  @GetMapping("/condition/header")
  Mono<String> headerCondition() {
    return Mono.just("Allowed");
  }

  @EndpointGate("param-condition-feature")
  @GetMapping("/condition/param")
  Mono<String> paramCondition() {
    return Mono.just("Allowed");
  }

  @EndpointGate("condition-rollout-feature")
  @GetMapping("/condition/with-rollout")
  Mono<String> conditionWithRollout() {
    return Mono.just("Allowed");
  }

  @EndpointGate("remote-address-condition-feature")
  @GetMapping("/condition/remote-address")
  Mono<String> remoteAddressCondition() {
    return Mono.just("Allowed");
  }

  public EndpointGateConditionController() {}
}
