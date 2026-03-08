package net.brightroom.example.provider.simple;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ui")
public class UiController {

  @GetMapping("/dark-mode")
  @EndpointGate("dark-mode")
  public String darkMode() {
    return "Dark mode is enabled";
  }
}
