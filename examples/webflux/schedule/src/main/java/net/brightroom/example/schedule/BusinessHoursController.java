package net.brightroom.example.schedule;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class BusinessHoursController {

  @EndpointGate("business-hours")
  @GetMapping("/api/support/chat")
  public Mono<String> supportChat() {
    return Mono.just("Support chat is available");
  }
}
