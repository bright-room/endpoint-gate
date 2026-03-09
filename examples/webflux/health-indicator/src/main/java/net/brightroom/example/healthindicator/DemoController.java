package net.brightroom.example.healthindicator;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class DemoController {

  @GetMapping("/active")
  @EndpointGate("active-feature")
  public Mono<String> active() {
    return Mono.just("active-feature is enabled!");
  }

  @GetMapping("/inactive")
  @EndpointGate("inactive-feature")
  public Mono<String> inactive() {
    return Mono.just("inactive-feature is enabled!");
  }
}
