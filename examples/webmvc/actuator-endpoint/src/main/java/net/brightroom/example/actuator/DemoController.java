package net.brightroom.example.actuator;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

  @GetMapping("/api/demo")
  @EndpointGate("demo-feature")
  public String demo() {
    return "demo-feature is enabled!";
  }

  @GetMapping("/api/another")
  @EndpointGate("another-feature")
  public String another() {
    return "another-feature is enabled!";
  }
}
