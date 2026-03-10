package net.brightroom.example.condition;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

  @EndpointGate("admin-panel")
  @GetMapping("/api/admin/dashboard")
  public String adminDashboard() {
    return "Admin dashboard";
  }
}
