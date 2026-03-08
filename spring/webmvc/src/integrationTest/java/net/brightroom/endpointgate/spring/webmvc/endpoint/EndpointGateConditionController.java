package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointGateConditionController {

  @EndpointGate("header-condition-gate")
  @GetMapping("/condition/header")
  String headerCondition() {
    return "Allowed";
  }

  @EndpointGate("param-condition-gate")
  @GetMapping("/condition/param")
  String paramCondition() {
    return "Allowed";
  }

  @EndpointGate("condition-rollout-gate")
  @GetMapping("/condition/with-rollout")
  String conditionWithRollout() {
    return "Allowed";
  }

  @EndpointGate("remote-address-condition-gate")
  @GetMapping("/condition/remote-address")
  String remoteAddressCondition() {
    return "Allowed";
  }

  public EndpointGateConditionController() {}
}
