package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for verifying fail-closed / fail-open behavior.
 *
 * <p>The gate {@code "undefined-in-config-gate"} is intentionally absent from {@code
 * endpoint-gate.gates} in {@code application.yaml}, so its enabled state is determined solely by
 * {@code endpoint-gate.default-enabled}.
 */
@RestController
public class EndpointGateUndefinedGateController {

  @EndpointGate("undefined-in-config-gate")
  @GetMapping("/undefined-gate-endpoint")
  String undefinedGateEndpoint() {
    return "Allowed";
  }

  public EndpointGateUndefinedGateController() {}
}
