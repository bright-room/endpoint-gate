package net.brightroom.example.basicusage.annotation;

import java.util.List;
import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

  @GetMapping("/search")
  @EndpointGate("new-search")
  public String search() {
    return "New search results";
  }

  @GetMapping("/export")
  @EndpointGate("new-export")
  public String export() {
    return "Export data";
  }

  @GetMapping("/list")
  public List<String> list() {
    return List.of("Alice", "Bob", "Charlie");
  }
}
