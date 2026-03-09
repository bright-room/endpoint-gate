package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class PremiumController {

  @GetMapping("/premium")
  @EndpointGate("premium-feature")
  public Mono<String> premium() {
    return Mono.just("Premium feature is enabled");
  }
}
