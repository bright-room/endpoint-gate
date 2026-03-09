package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DashboardController {

  @GetMapping("/dashboard")
  @EndpointGate("new-dashboard")
  public Mono<String> dashboard() {
    return Mono.just("New dashboard is enabled");
  }
}
