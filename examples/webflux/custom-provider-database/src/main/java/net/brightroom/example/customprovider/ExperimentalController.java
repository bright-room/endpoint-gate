package net.brightroom.example.customprovider;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ExperimentalController {

  @EndpointGate("experimental")
  @GetMapping("/api/experimental")
  public Mono<String> experimental() {
    return Mono.just("Experimental feature");
  }

  @EndpointGate("development")
  @GetMapping("/api/development")
  public Mono<String> development() {
    return Mono.just("Development feature");
  }
}
