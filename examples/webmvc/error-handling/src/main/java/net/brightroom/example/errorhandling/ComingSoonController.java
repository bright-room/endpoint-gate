package net.brightroom.example.errorhandling;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ComingSoonController {

  @GetMapping("/coming-soon")
  public String comingSoon() {
    return "This feature is coming soon. Please check back later!";
  }
}
