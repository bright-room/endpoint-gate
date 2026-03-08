package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateRouterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifies fail-open behavior for Functional Endpoints: when {@code endpoint-gate.default-enabled}
 * is {@code true}, requests to routes whose gate is absent from {@code endpoint-gate.gates} in
 * {@code application.yaml} are allowed through.
 */
@WebFluxTest(properties = {"endpoint-gate.default-enabled=true"})
@Import({EndpointGateWebFluxTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionFailOpenIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldAllowAccess_whenGateIsUndefinedInConfig_andDefaultEnabledIsTrue() {
    webTestClient
        .get()
        .uri("/functional/undefined-flag-endpoint")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Autowired
  EndpointGateHandlerFilterFunctionFailOpenIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
