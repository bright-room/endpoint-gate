package net.brightroom.endpointgate.spring.webflux.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Test controller for verifying fail-closed / fail-open behavior.
 *
 * <p>The gate {@code "undefined-in-config-flag"} is intentionally absent from {@code
 * endpoint-gate.gates} in {@code application.yaml}, so its enabled state is determined solely by
 * {@code endpoint-gate.default-enabled}.
 */
@RestController
public class EndpointGateUndefinedGateController {

  @EndpointGate("undefined-in-config-flag")
  @GetMapping("/undefined-flag-endpoint")
  Mono<String> undefinedFlagEndpoint() {
    return Mono.just("Allowed");
  }

  public EndpointGateUndefinedGateController() {}
}
