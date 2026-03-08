package net.brightroom.example.failbehavior;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fail-open")
public class FailOpenController {

  @GetMapping("/known-disabled")
  @EndpointGate("known-disabled")
  public String knownDisabled() {
    return "This should not be reached (explicitly disabled)";
  }

  @GetMapping("/unknown")
  @EndpointGate("undefined-feature")
  public String unknown() {
    return "Undefined feature is allowed (fail-open)";
  }
}
