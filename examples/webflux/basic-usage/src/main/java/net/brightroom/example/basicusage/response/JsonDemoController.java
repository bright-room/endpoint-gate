package net.brightroom.example.basicusage.response;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** R-1: JSON response type (default) — denied response returns application/problem+json. */
@RestController
@RequestMapping("/response")
public class JsonDemoController {

  @GetMapping("/json")
  @EndpointGate("json-demo")
  public Mono<String> json() {
    return Mono.just("JSON demo response");
  }
}
