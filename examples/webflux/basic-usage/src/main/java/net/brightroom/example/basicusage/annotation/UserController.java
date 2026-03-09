package net.brightroom.example.basicusage.annotation;

import java.util.List;
import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A-1: No @EndpointGate — always accessible (/users/list).
 *
 * <p>A-4: Method-level @EndpointGate — per-method control.
 *
 * <p>A-8: Mono return type (/users/search).
 *
 * <p>A-9: Flux return type (/users/list).
 */
@RestController
@RequestMapping("/users")
public class UserController {

  @GetMapping("/search")
  @EndpointGate("new-search")
  public Mono<String> search() {
    return Mono.just("New search results");
  }

  @GetMapping("/export")
  @EndpointGate("new-export")
  public Mono<String> export() {
    return Mono.just("Export data");
  }

  @GetMapping("/list")
  public Flux<String> list() {
    return Flux.fromIterable(List.of("Alice", "Bob", "Charlie"));
  }
}
