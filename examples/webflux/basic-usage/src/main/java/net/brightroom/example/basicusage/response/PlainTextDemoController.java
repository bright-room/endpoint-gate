package net.brightroom.example.basicusage.response;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** R-3: PLAIN_TEXT response type — denied response returns text/plain. */
@RestController
@RequestMapping("/response")
public class PlainTextDemoController {

  @GetMapping("/plain-text")
  @EndpointGate("plain-text-demo")
  public Mono<String> plainText() {
    return Mono.just("Plain text demo response");
  }
}
