package net.brightroom.example.errorhandling;

import net.brightroom.endpointgate.spring.webmvc.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Functional endpoint routing configuration demonstrating custom
 * AccessDeniedHandlerFilterResolution.
 *
 * <p>When the endpoint gate is disabled, the custom resolution bean provides the denied response
 * instead of the default. Note: @ControllerAdvice is NOT used for functional endpoints.
 */
@Configuration
@Profile("functional-error")
public class ErrorHandlingRouter {

  /**
   * Registers functional endpoint routes with endpoint gate filter.
   *
   * @param handler the request handler
   * @param endpointGateFilter the endpoint gate filter function factory
   * @return the router function
   */
  @Bean
  public RouterFunction<ServerResponse> errorHandlingRoutes(
      ErrorHandlingHandler handler, EndpointGateHandlerFilterFunction endpointGateFilter) {
    return RouterFunctions.route()
        .GET("/api/functional/gated", handler::gated)
        .filter(endpointGateFilter.of("functional-gated"))
        .build();
  }
}
