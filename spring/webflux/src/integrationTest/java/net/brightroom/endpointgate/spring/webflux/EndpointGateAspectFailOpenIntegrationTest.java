package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateUndefinedGateController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifies fail-open behavior: when {@code endpoint-gate.default-enabled} is {@code true}, requests
 * to endpoints whose gate is absent from {@code endpoint-gate.gates} in {@code application.yaml}
 * are allowed through.
 */
@WebFluxTest(
    properties = {"endpoint-gate.default-enabled=true"},
    controllers = EndpointGateUndefinedGateController.class)
@Import(EndpointGateWebFluxTestAutoConfiguration.class)
class EndpointGateAspectFailOpenIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldAllowAccess_whenGateIsUndefinedInConfig_andDefaultEnabledIsTrue() {
    webTestClient
        .get()
        .uri("/undefined-flag-endpoint")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Autowired
  EndpointGateAspectFailOpenIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
