package net.brightroom.example.basicusage.annotation;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * A-3: Class-level disabled flag — all endpoints return 403.
 *
 * <p>A-5: Method-level flag overrides class-level — /legacy/special returns 200 OK.
 */
@RestController
@RequestMapping("/legacy")
@EndpointGate("legacy-api")
public class LegacyController {

  @GetMapping("/data")
  public Mono<String> data() {
    return Mono.just("Legacy data");
  }

  @GetMapping("/special")
  @EndpointGate("special-endpoint")
  public Mono<String> special() {
    return Mono.just("Special endpoint data");
  }
}
