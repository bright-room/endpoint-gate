package net.brightroom.endpointgate.spring.webmvc.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointGateScheduleController {

  @GetMapping("/schedule/active")
  @EndpointGate("active-scheduled-gate")
  public String activeSchedule() {
    return "Allowed";
  }

  @GetMapping("/schedule/inactive")
  @EndpointGate("inactive-scheduled-gate")
  public String inactiveSchedule() {
    return "Allowed";
  }

  @GetMapping("/schedule/timezone")
  @EndpointGate("timezone-scheduled-gate")
  public String timezoneSchedule() {
    return "Allowed";
  }

  @GetMapping("/schedule/end-only-inactive")
  @EndpointGate("end-only-inactive-scheduled-gate")
  public String endOnlyInactiveSchedule() {
    return "Allowed";
  }
}
