package net.brightroom.endpointgate.spring.metrics.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EndpointGate("disabled-gate")
public class MetricsDisabledController {

  @GetMapping("/metrics-test/disabled")
  String disabled() {
    return "Not Allowed";
  }
}
