package net.brightroom.example.failbehavior;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fail-closed")
public class FailClosedController {

  @GetMapping("/known")
  @EndpointGate("known-feature")
  public String known() {
    return "Known feature is enabled";
  }

  @GetMapping("/unknown")
  @EndpointGate("undefined-feature")
  public String unknown() {
    return "This should not be reached (fail-closed)";
  }
}
