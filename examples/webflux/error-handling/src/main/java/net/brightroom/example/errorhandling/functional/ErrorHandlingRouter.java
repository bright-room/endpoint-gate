package net.brightroom.example.errorhandling.functional;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import net.brightroom.endpointgate.spring.webflux.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Functional endpoint demonstrating C-3: custom AccessDeniedHandlerFilterResolution.
 *
 * <p>When the endpoint gate is disabled, the custom resolution bean provides the denied response
 * instead of the default. Note: @ControllerAdvice is NOT used for functional endpoints.
 */
@Configuration
public class ErrorHandlingRouter {

  @Bean
  public RouterFunction<ServerResponse> errorHandlingRoutes(
      ErrorHandlingHandler handler, EndpointGateHandlerFilterFunction endpointGateFilter) {

    return route(GET("/functional/premium"), handler::premiumFeature)
        .filter(endpointGateFilter.of("premium-feature"));
  }
}
