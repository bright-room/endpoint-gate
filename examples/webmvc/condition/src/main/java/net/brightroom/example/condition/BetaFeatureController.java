package net.brightroom.example.condition;

import net.brightroom.endpointgate.core.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BetaFeatureController {

  @EndpointGate("beta-feature")
  @GetMapping("/api/beta/feature")
  public String betaFeature() {
    return "Beta feature enabled";
  }
}
