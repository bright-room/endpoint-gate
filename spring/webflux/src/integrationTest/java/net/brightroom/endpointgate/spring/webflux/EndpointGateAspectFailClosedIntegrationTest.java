package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateUndefinedGateController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifies fail-closed behavior: when {@code endpoint-gate.default-enabled} is {@code false},
 * requests to endpoints whose gate is absent from {@code endpoint-gate.gates} in {@code
 * application.yaml} are blocked with {@code 403 Forbidden}.
 */
@WebFluxTest(
    properties = {"endpoint-gate.default-enabled=false"},
    controllers = EndpointGateUndefinedGateController.class)
@Import(EndpointGateWebFluxTestAutoConfiguration.class)
class EndpointGateAspectFailClosedIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldBlockAccess_whenGateIsUndefinedInConfig_andDefaultEnabledIsFalse() {
    webTestClient
        .get()
        .uri("/undefined-flag-endpoint")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail"   : "Gate 'undefined-in-config-flag' is not available",
              "instance" : "/undefined-flag-endpoint",
              "status"   : 403,
              "title"    : "Endpoint gate access denied",
              "type"     : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Autowired
  EndpointGateAspectFailClosedIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
