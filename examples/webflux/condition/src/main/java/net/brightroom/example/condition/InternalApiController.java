package net.brightroom.example.condition;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Header-based condition example. Requires {@code X-Internal-Token} header to access. */
@RestController
public class InternalApiController {

  /**
   * Returns internal data when the {@code X-Internal-Token} header matches the expected value.
   *
   * @return internal data response
   */
  @EndpointGate("internal-api")
  @GetMapping("/api/internal/data")
  public Mono<String> getData() {
    return Mono.just("Internal data");
  }
}
