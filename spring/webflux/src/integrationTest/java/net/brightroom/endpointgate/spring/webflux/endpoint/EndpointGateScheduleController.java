package net.brightroom.endpointgate.spring.webflux.endpoint;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class EndpointGateScheduleController {

  @GetMapping("/schedule/active")
  @EndpointGate("active-scheduled-gate")
  public Mono<String> activeSchedule() {
    return Mono.just("Allowed");
  }

  @GetMapping("/schedule/inactive")
  @EndpointGate("inactive-scheduled-gate")
  public Mono<String> inactiveSchedule() {
    return Mono.just("Allowed");
  }

  @GetMapping("/schedule/timezone")
  @EndpointGate("timezone-scheduled-gate")
  public Mono<String> timezoneSchedule() {
    return Mono.just("Allowed");
  }

  @GetMapping("/schedule/end-only-inactive")
  @EndpointGate("end-only-inactive-scheduled-gate")
  public Mono<String> endOnlyInactiveSchedule() {
    return Mono.just("Allowed");
  }
}
