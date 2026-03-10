package net.brightroom.example.basicusage.annotation;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates multiple gate IDs with AND semantics.
 *
 * <p>All specified gates must permit access; if any gate denies access, the request is rejected.
 */
@RestController
@RequestMapping("/multi")
public class MultipleGatesController {

  @GetMapping("/dashboard")
  @EndpointGate({"feature-new-dashboard", "beta-users-only"})
  public String dashboard() {
    return "New Dashboard";
  }
}
