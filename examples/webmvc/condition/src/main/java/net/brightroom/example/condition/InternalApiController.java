package net.brightroom.example.condition;

import net.brightroom.endpointgate.core.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalApiController {

  @EndpointGate("internal-api")
  @GetMapping("/api/internal/data")
  public String getData() {
    return "Internal data";
  }
}
