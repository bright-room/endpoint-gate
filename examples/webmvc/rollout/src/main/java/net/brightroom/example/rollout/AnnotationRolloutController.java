package net.brightroom.example.rollout;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnnotationRolloutController {

  @GetMapping("/api/annotation-rollout")
  @EndpointGate("annotation-rollout")
  public String annotationRollout() {
    return "annotation-rollout: you are in the rollout group!";
  }
}
