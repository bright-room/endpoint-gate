package net.brightroom.example.basicusage.functional;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import net.brightroom.endpointgate.spring.webflux.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * F-1: No filter — always accessible.
 *
 * <p>F-2: Filter with enabled flag — 200 OK.
 *
 * <p>F-3: Filter with disabled flag — 403 Forbidden.
 */
@Configuration
public class FunctionalEndpointRouter {

  @Bean
  public RouterFunction<ServerResponse> functionalRoutes(
      FunctionalEndpointHandler handler, EndpointGateHandlerFilterFunction endpointGateFilter) {

    // F-1: No filter applied
    RouterFunction<ServerResponse> noFilterRoute =
        route(GET("/functional/no-filter"), handler::noFilter);

    // F-2: Enabled flag filter — returns 200 OK
    RouterFunction<ServerResponse> enabledRoute =
        route(GET("/functional/enabled"), handler::enabledFeature)
            .filter(endpointGateFilter.of("enabled-flag"));

    // F-3: Disabled flag filter — returns 403 Forbidden
    RouterFunction<ServerResponse> disabledRoute =
        route(GET("/functional/disabled"), handler::disabledFeature)
            .filter(endpointGateFilter.of("disabled-flag"));

    return noFilterRoute.and(enabledRoute).and(disabledRoute);
  }
}
