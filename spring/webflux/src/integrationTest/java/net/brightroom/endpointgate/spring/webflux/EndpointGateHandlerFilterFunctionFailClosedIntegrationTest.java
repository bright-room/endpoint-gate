package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateRouterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifies fail-closed behavior for Functional Endpoints: when {@code
 * endpoint-gate.default-enabled} is {@code false}, requests to routes whose gate is absent from
 * {@code endpoint-gate.gates} in {@code application.yaml} are blocked with {@code 403 Forbidden}.
 */
@WebFluxTest(properties = {"endpoint-gate.default-enabled=false"})
@Import({EndpointGateWebFluxTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionFailClosedIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldBlockAccess_whenGateIsUndefinedInConfig_andDefaultEnabledIsFalse() {
    webTestClient
        .get()
        .uri("/functional/undefined-flag-endpoint")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail"   : "Gate 'undefined-in-config-flag' is not available",
              "instance" : "/functional/undefined-flag-endpoint",
              "status"   : 403,
              "title"    : "Endpoint gate access denied",
              "type"     : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Autowired
  EndpointGateHandlerFilterFunctionFailClosedIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
