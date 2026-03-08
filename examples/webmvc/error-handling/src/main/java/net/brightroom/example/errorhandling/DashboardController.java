package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

  @GetMapping("/dashboard")
  @EndpointGate("new-dashboard")
  public String dashboard() {
    return "New dashboard is enabled";
  }
}
