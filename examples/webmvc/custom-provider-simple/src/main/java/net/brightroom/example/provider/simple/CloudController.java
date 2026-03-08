package net.brightroom.example.provider.simple;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CloudController {

  @GetMapping("/cloud")
  @EndpointGate("cloud-feature")
  public String cloud() {
    return "Cloud feature is enabled";
  }

  @GetMapping("/beta")
  @EndpointGate("beta-feature")
  public String beta() {
    return "Beta feature is enabled";
  }
}
