package net.brightroom.example.functional;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import net.brightroom.endpointgate.spring.webflux.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RoutingConfiguration {

  private final EndpointGateHandlerFilterFunction endpointGateFilter;

  public RoutingConfiguration(EndpointGateHandlerFilterFunction endpointGateFilter) {
    this.endpointGateFilter = endpointGateFilter;
  }

  @Bean
  RouterFunction<ServerResponse> stableRoute(FeatureHandler handler) {
    return route()
        .GET("/api/stable", handler::stable)
        .filter(endpointGateFilter.of("stable-api"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> betaRoute(FeatureHandler handler) {
    return route()
        .GET("/api/beta", handler::beta)
        .filter(endpointGateFilter.of("beta-api", 50))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> experimentalRoute(FeatureHandler handler) {
    return route()
        .GET("/api/experimental", handler::experimental)
        .filter(endpointGateFilter.of("experimental-api"))
        .build();
  }
}
