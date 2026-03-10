package net.brightroom.endpointgate.spring.webmvc.endpoint;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;

import net.brightroom.endpointgate.spring.webmvc.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class EndpointGateRouterConfiguration {

  private final EndpointGateHandlerFilterFunction endpointGateFilter;

  @Bean
  RouterFunction<ServerResponse> functionalStableRoute() {
    return route(
        GET("/functional/stable-endpoint"), req -> ServerResponse.ok().body("No Annotation"));
  }

  @Bean
  RouterFunction<ServerResponse> functionalEnabledRoute() {
    return route()
        .GET("/functional/experimental-stage-endpoint", req -> ServerResponse.ok().body("Allowed"))
        .filter(endpointGateFilter.of("experimental-stage-endpoint"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalDisabledRoute() {
    return route()
        .GET(
            "/functional/development-stage-endpoint",
            req -> ServerResponse.ok().body("Not Allowed"))
        .filter(endpointGateFilter.of("development-stage-endpoint"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalDisabledGroupRoute() {
    return route()
        .GET("/functional/test/disable", req -> ServerResponse.ok().body("Not Allowed"))
        .filter(endpointGateFilter.of("disable-class-level-feature"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalEnabledGroupRoute() {
    return route()
        .GET("/functional/test/enabled", req -> ServerResponse.ok().body("Allowed"))
        .filter(endpointGateFilter.of("enable-class-level-feature"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalUndefinedGateRoute() {
    return route()
        .GET("/functional/undefined-gate-endpoint", req -> ServerResponse.ok().body("Allowed"))
        .filter(endpointGateFilter.of("undefined-in-config-gate"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalConditionRoute() {
    return route()
        .GET("/functional/condition/header", req -> ServerResponse.ok().body("Allowed"))
        .filter(
            endpointGateFilter.withConditionFallback(
                "conditional-gate", "headers['X-Beta'] != null"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalMultipleGatesAllEnabledRoute() {
    return route()
        .GET("/functional/multiple-gates/all-enabled", req -> ServerResponse.ok().body("Allowed"))
        .filter(endpointGateFilter.of("gate-a", "gate-b"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalMultipleGatesOneDisabledRoute() {
    return route()
        .GET(
            "/functional/multiple-gates/one-disabled",
            req -> ServerResponse.ok().body("Not Allowed"))
        .filter(endpointGateFilter.of("gate-a", "gate-disabled"))
        .build();
  }

  public EndpointGateRouterConfiguration(EndpointGateHandlerFilterFunction endpointGateFilter) {
    this.endpointGateFilter = endpointGateFilter;
  }
}
