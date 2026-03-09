package net.brightroom.example.condition;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Query parameter-based condition example. Requires {@code beta=true} query parameter to access. */
@RestController
public class BetaFeatureController {

  /**
   * Returns beta feature content when the {@code beta} query parameter equals {@code true}.
   *
   * @return beta feature response
   */
  @EndpointGate("beta-feature")
  @GetMapping("/api/beta/feature")
  public Mono<String> betaFeature() {
    return Mono.just("Beta feature enabled");
  }
}
