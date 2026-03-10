package net.brightroom.example.schedule;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MaintenanceController {

  @EndpointGate("maintenance-window")
  @GetMapping("/api/maintenance/status")
  public String maintenanceStatus() {
    return "System is under maintenance";
  }
}
