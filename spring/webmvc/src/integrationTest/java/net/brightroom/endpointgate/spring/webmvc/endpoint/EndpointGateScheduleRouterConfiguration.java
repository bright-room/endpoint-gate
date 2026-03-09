package net.brightroom.endpointgate.spring.webmvc.endpoint;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;

import net.brightroom.endpointgate.spring.webmvc.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class EndpointGateScheduleRouterConfiguration {

  private final EndpointGateHandlerFilterFunction endpointGateFilter;

  @Bean
  RouterFunction<ServerResponse> functionalActiveScheduleRoute() {
    return route()
        .GET("/functional/schedule/active", req -> ServerResponse.ok().body("Allowed"))
        .filter(endpointGateFilter.of("active-scheduled-gate"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalInactiveScheduleRoute() {
    return route()
        .GET("/functional/schedule/inactive", req -> ServerResponse.ok().body("Allowed"))
        .filter(endpointGateFilter.of("inactive-scheduled-gate"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalEndOnlyInactiveScheduleRoute() {
    return route()
        .GET("/functional/schedule/end-only-inactive", req -> ServerResponse.ok().body("Allowed"))
        .filter(endpointGateFilter.of("end-only-inactive-scheduled-gate"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalTimezoneScheduleRoute() {
    return route()
        .GET("/functional/schedule/timezone", req -> ServerResponse.ok().body("Allowed"))
        .filter(endpointGateFilter.of("timezone-scheduled-gate"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalStableScheduleRoute() {
    return route(GET("/functional/schedule/stable"), req -> ServerResponse.ok().body("No Filter"));
  }

  public EndpointGateScheduleRouterConfiguration(
      EndpointGateHandlerFilterFunction endpointGateFilter) {
    this.endpointGateFilter = endpointGateFilter;
  }
}
