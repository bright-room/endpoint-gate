package net.brightroom.example.basicusage.response;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/response")
public class HtmlDemoController {

  @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
  @EndpointGate("html-demo")
  public String html() {
    return "<html><body><h1>HTML demo response</h1></body></html>";
  }
}
