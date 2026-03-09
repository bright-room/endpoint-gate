package net.brightroom.example.failbehavior.functional;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import net.brightroom.endpointgate.spring.webflux.filter.EndpointGateHandlerFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * F-4: Undefined flag with Fail-Closed — returns 403.
 *
 * <p>F-5: Undefined flag with Fail-Open — returns 200 (requires fail-open profile).
 */
@Configuration
public class FailBehaviorRouter {

  @Bean
  public RouterFunction<ServerResponse> failBehaviorRoutes(
      FailBehaviorHandler handler, EndpointGateHandlerFilterFunction endpointGateFilter) {

    // Known feature — enabled in config
    RouterFunction<ServerResponse> knownRoute =
        route(GET("/functional/fail-closed/known"), handler::knownFeature)
            .filter(endpointGateFilter.of("known-feature"));

    // Undefined feature — behavior depends on default-enabled setting
    RouterFunction<ServerResponse> undefinedFailClosedRoute =
        route(GET("/functional/fail-closed/unknown"), handler::undefinedFeature)
            .filter(endpointGateFilter.of("undefined-feature"));

    // Known disabled feature (fail-open profile)
    RouterFunction<ServerResponse> knownDisabledRoute =
        route(GET("/functional/fail-open/known-disabled"), handler::knownDisabledFeature)
            .filter(endpointGateFilter.of("known-disabled"));

    // Undefined feature under fail-open
    RouterFunction<ServerResponse> undefinedFailOpenRoute =
        route(GET("/functional/fail-open/unknown"), handler::undefinedFeature)
            .filter(endpointGateFilter.of("undefined-feature"));

    return knownRoute
        .and(undefinedFailClosedRoute)
        .and(knownDisabledRoute)
        .and(undefinedFailOpenRoute);
  }
}
