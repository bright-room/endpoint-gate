package net.brightroom.example.errorhandling;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ComingSoonController {

  @GetMapping("/coming-soon")
  public Mono<String> comingSoon() {
    return Mono.just("This feature is coming soon. Please check back later!");
  }
}
