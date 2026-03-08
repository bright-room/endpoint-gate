package net.brightroom.endpointgate.spring.webmvc.endpoint;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NoEndpointGateViewController {

  @GetMapping("/view/test/no-annotation")
  String noAnnotation() {
    return "no-annotation";
  }
}
