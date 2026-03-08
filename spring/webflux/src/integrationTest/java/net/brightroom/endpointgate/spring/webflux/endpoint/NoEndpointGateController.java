package net.brightroom.endpointgate.spring.webflux.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class NoEndpointGateController {

  @GetMapping("/test/no-annotation")
  Mono<String> noAnnotation() {
    return Mono.just("No Annotation");
  }

  public NoEndpointGateController() {}
}
