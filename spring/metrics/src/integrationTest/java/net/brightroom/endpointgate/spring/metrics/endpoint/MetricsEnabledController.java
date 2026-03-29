package net.brightroom.endpointgate.spring.metrics.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EndpointGate("enabled-gate")
public class MetricsEnabledController {

  @GetMapping("/metrics-test/enabled")
  String enabled() {
    return "Allowed";
  }
}
