package net.brightroom.example.schedule;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class MaintenanceController {

  @EndpointGate("maintenance-window")
  @GetMapping("/api/maintenance/status")
  public Mono<String> maintenanceStatus() {
    return Mono.just("System is under maintenance");
  }
}
