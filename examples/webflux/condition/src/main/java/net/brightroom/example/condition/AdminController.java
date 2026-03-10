package net.brightroom.example.condition;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Role-based condition example. Requires {@code X-User-Role: admin} header to access. */
@RestController
public class AdminController {

  /**
   * Returns admin dashboard content when the {@code X-User-Role} header equals {@code admin}.
   *
   * @return admin dashboard response
   */
  @EndpointGate("admin-panel")
  @GetMapping("/api/admin/dashboard")
  public Mono<String> adminDashboard() {
    return Mono.just("Admin dashboard");
  }
}
