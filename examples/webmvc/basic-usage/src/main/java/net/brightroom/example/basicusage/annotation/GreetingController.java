package net.brightroom.example.basicusage.annotation;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EndpointGate("greeting")
public class GreetingController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello, World!";
  }

  @GetMapping("/goodbye")
  public String goodbye() {
    return "Goodbye, World!";
  }
}
