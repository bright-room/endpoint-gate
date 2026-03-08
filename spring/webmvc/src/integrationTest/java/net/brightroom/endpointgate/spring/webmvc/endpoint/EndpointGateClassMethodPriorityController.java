package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/legacy")
@EndpointGate("legacy-api")
public class EndpointGateClassMethodPriorityController {

  @GetMapping("/data")
  public String data() {
    return "Legacy data";
  }

  @EndpointGate("special-endpoint")
  @GetMapping("/special")
  public String special() {
    return "Special endpoint data";
  }
}
