package net.brightroom.endpointgate.spring.metrics.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@EndpointGate("disabled-gate")
public class ReactiveMetricsDisabledController {

  @GetMapping("/metrics-test/disabled")
  Mono<String> disabled() {
    return Mono.just("Not Allowed");
  }
}
