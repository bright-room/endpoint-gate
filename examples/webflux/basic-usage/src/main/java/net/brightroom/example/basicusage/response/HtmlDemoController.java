package net.brightroom.example.basicusage.response;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** R-2: HTML response type — denied response returns text/html. */
@RestController
@RequestMapping("/response")
public class HtmlDemoController {

  @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
  @EndpointGate("html-demo")
  public Mono<String> html() {
    return Mono.just("<html><body><h1>HTML demo response</h1></body></html>");
  }
}
