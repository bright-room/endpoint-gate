package net.brightroom.endpointgate.spring.metrics.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@EndpointGate("enabled-gate")
public class ReactiveMetricsEnabledController {

  @GetMapping("/metrics-test/enabled")
  Mono<String> enabled() {
    return Mono.just("Allowed");
  }
}
