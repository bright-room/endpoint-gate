package net.brightroom.example.failbehavior;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * A-7: Undefined flag with default-enabled: true (Fail-Open) — returns 200.
 *
 * <p>Requires profile: fail-open (sets default-enabled: true).
 */
@RestController
@RequestMapping("/fail-open")
public class FailOpenController {

  @GetMapping("/known-disabled")
  @EndpointGate("known-disabled")
  public Mono<String> knownDisabled() {
    return Mono.just("This should not be reached (explicitly disabled)");
  }

  @GetMapping("/unknown")
  @EndpointGate("undefined-feature")
  public Mono<String> unknown() {
    return Mono.just("Undefined feature is allowed (fail-open)");
  }
}
