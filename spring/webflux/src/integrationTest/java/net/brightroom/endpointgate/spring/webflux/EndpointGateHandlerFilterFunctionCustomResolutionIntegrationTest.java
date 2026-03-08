package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateRouterConfiguration;
import net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

@WebFluxTest
@Import({
  EndpointGateHandlerFilterFunctionCustomResolutionIntegrationTest.CustomResolutionConfiguration
      .class,
  EndpointGateWebFluxTestAutoConfiguration.class,
  EndpointGateRouterConfiguration.class,
})
class EndpointGateHandlerFilterFunctionCustomResolutionIntegrationTest {

  @Configuration
  static class CustomResolutionConfiguration {

    @Bean
    @Primary
    AccessDeniedHandlerFilterResolution customResolution() {
      return (request, e) ->
          ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).bodyValue("custom: " + e.gateId());
    }
  }

  WebTestClient webTestClient;

  @Test
  void customResolutionTakesPriority_whenGateIsDisabled() {
    webTestClient
        .get()
        .uri("/functional/development-stage-endpoint")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
        .expectBody(String.class)
        .isEqualTo("custom: development-stage-endpoint");
  }

  @Test
  void customResolutionTakesPriority_whenClassLevelGateIsDisabled() {
    webTestClient
        .get()
        .uri("/functional/test/disable")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
        .expectBody(String.class)
        .isEqualTo("custom: disable-class-level-feature");
  }

  @Autowired
  EndpointGateHandlerFilterFunctionCustomResolutionIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
