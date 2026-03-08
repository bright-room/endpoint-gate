package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PremiumController {

  @GetMapping("/premium")
  @EndpointGate("premium-feature")
  public String premium() {
    return "Premium feature is enabled";
  }
}
