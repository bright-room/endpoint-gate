package net.brightroom.example.customprovider;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class DemoController {

  @GetMapping("/reactive-feature")
  @EndpointGate("reactive-feature")
  public Mono<String> reactiveFeature() {
    return Mono.just("reactive-feature is enabled via custom provider!");
  }

  @GetMapping("/beta-feature")
  @EndpointGate("beta-feature")
  public Mono<String> betaFeature() {
    return Mono.just("beta-feature is enabled via custom provider!");
  }
}
