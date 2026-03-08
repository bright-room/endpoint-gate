package net.brightroom.example.basicusage.response;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/response")
public class PlainTextDemoController {

  @GetMapping("/plain-text")
  @EndpointGate("plain-text-demo")
  public String plainText() {
    return "Plain text demo response";
  }
}
