package net.brightroom.example.functional;

import static org.springframework.web.servlet.function.RouterFunctions.route;

import net.brightroom.endpointgate.spring.webmvc.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

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
        .filter(endpointGateFilter.of("beta-api"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> experimentalRoute(FeatureHandler handler) {
    return route()
        .GET("/api/experimental", handler::experimental)
        .filter(endpointGateFilter.of("experimental-api"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> internalRoute(FeatureHandler handler) {
    return route()
        .GET("/api/internal", handler::internal)
        .filter(endpointGateFilter.of("internal-api"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> previewRoute(FeatureHandler handler) {
    return route()
        .GET("/api/preview", handler::preview)
        .filter(endpointGateFilter.of("preview-feature"))
        .build();
  }

  @Bean
  RouterFunction<ServerResponse> multiGateRoute(FeatureHandler handler) {
    return route()
        .GET("/api/restricted", handler::restricted)
        .filter(endpointGateFilter.of("stable-api", "beta-api"))
        .build();
  }
}
