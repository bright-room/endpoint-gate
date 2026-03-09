package net.brightroom.example.basicusage.annotation;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** A-2: Class-level @EndpointGate with enabled flag — all endpoints return 200 OK. */
@RestController
@EndpointGate("greeting")
public class GreetingController {

  @GetMapping("/hello")
  public Mono<String> hello() {
    return Mono.just("Hello, World!");
  }

  @GetMapping("/goodbye")
  public Mono<String> goodbye() {
    return Mono.just("Goodbye, World!");
  }
}
