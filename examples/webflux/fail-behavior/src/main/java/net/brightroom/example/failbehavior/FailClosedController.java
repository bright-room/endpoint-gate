package net.brightroom.example.failbehavior;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * A-6: Undefined gate with default-enabled: false (Fail-Closed) — returns 403.
 *
 * <p>Default profile: endpoint-gate.default-enabled is false (default).
 */
@RestController
@RequestMapping("/fail-closed")
public class FailClosedController {

  @GetMapping("/known")
  @EndpointGate("known-feature")
  public Mono<String> known() {
    return Mono.just("Known feature is enabled");
  }

  @GetMapping("/unknown")
  @EndpointGate("undefined-feature")
  public Mono<String> unknown() {
    return Mono.just("This should not be reached (fail-closed)");
  }
}
