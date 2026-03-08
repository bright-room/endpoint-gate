package net.brightroom.endpointgate.spring.webmvc.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoEndpointGateController {

  @GetMapping("/test/no-annotation")
  String noAnnotation() {
    return "No Annotation";
  }
}
