package net.brightroom.endpointgate.spring.webflux.endpoint;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import net.brightroom.endpointgate.spring.webflux.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class EndpointGateRouterConfiguration {

  private final EndpointGateHandlerFilterFunction endpointGateFilter;

  @Bean
  RouterFunction<ServerResponse> functionalStableRoute() {
    return route(
        GET("/functional/stable-endpoint"), req -> ServerResponse.ok().bodyValue("No Annotation"));
  }

  @Bean
  RouterFunction<ServerResponse> functionalEnabledRoute() {
    return route()
        .GET(
            "/functional/experimental-stage-endpoint",
            req -> ServerResponse.ok().bodyValue("Allowed"))
        .filter(endpointGateFilter.of("experimental-stage-endpoint"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalDisabledRoute() {
    return route()
        .GET(
            "/functional/development-stage-endpoint",
            req -> ServerResponse.ok().bodyValue("Not Allowed"))
        .filter(endpointGateFilter.of("development-stage-endpoint"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalClassLevelDisabledRoute() {
    return route()
        .GET("/functional/test/disable", req -> ServerResponse.ok().bodyValue("Not Allowed"))
        .filter(endpointGateFilter.of("disable-class-level-feature"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalClassLevelEnabledRoute() {
    return route()
        .GET("/functional/test/enabled", req -> ServerResponse.ok().bodyValue("Allowed"))
        .filter(endpointGateFilter.of("enable-class-level-feature"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalUndefinedFlagRoute() {
    return route()
        .GET("/functional/undefined-flag-endpoint", req -> ServerResponse.ok().bodyValue("Allowed"))
        .filter(endpointGateFilter.of("undefined-in-config-flag"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalConditionRoute() {
    return route()
        .GET("/functional/condition/header", req -> ServerResponse.ok().bodyValue("Allowed"))
        .filter(endpointGateFilter.of("conditional-header-feature"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalConditionParamRoute() {
    return route()
        .GET("/functional/condition/param", req -> ServerResponse.ok().bodyValue("Allowed"))
        .filter(endpointGateFilter.of("conditional-param-feature"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalMultipleGatesAllEnabledRoute() {
    return route()
        .GET(
            "/functional/multiple-gates/all-enabled",
            req -> ServerResponse.ok().bodyValue("Allowed"))
        .filter(endpointGateFilter.of("gate-a", "gate-b"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> functionalMultipleGatesOneDisabledRoute() {
    return route()
        .GET(
            "/functional/multiple-gates/one-disabled",
            req -> ServerResponse.ok().bodyValue("Not Allowed"))
        .filter(endpointGateFilter.of("gate-a", "gate-disabled"))
        .build();
  }

  public EndpointGateRouterConfiguration(EndpointGateHandlerFilterFunction endpointGateFilter) {
    this.endpointGateFilter = endpointGateFilter;
  }
}
