package net.brightroom.example.schedule;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BusinessHoursController {

  @EndpointGate("business-hours")
  @GetMapping("/api/support/chat")
  public String supportChat() {
    return "Support chat is available";
  }
}
