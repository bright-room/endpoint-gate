package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateRouterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@Import({EndpointGateWebFluxTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionMultipleGatesIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldAllowAccess_whenAllGatesAreEnabled() {
    webTestClient
        .get()
        .uri("/functional/multiple-gates/all-enabled")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldBlockAccess_whenOneGateIsDisabled() {
    webTestClient
        .get()
        .uri("/functional/multiple-gates/one-disabled")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail" : "Gate 'gate-disabled' is not available",
              "instance" : "/functional/multiple-gates/one-disabled",
              "status" : 403,
              "title" : "Endpoint gate access denied",
              "type" : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Autowired
  EndpointGateHandlerFilterFunctionMultipleGatesIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
