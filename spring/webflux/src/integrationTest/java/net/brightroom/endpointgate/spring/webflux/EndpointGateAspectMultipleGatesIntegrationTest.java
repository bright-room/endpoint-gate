package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateMultipleGatesController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = {EndpointGateMultipleGatesController.class})
@Import(EndpointGateWebFluxTestAutoConfiguration.class)
class EndpointGateAspectMultipleGatesIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldAllowAccess_whenAllGatesAreEnabled() {
    webTestClient
        .get()
        .uri("/multiple-gates/all-enabled")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldBlockAccess_whenSecondGateIsDisabled() {
    webTestClient
        .get()
        .uri("/multiple-gates/one-disabled")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail" : "Gate 'gate-disabled' is not available",
              "instance" : "/multiple-gates/one-disabled",
              "status" : 403,
              "title" : "Endpoint gate access denied",
              "type" : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Test
  void shouldBlockAccess_withFirstDisabledGateId_whenFirstGateIsDisabled() {
    webTestClient
        .get()
        .uri("/multiple-gates/first-disabled")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail" : "Gate 'gate-disabled' is not available",
              "instance" : "/multiple-gates/first-disabled",
              "status" : 403,
              "title" : "Endpoint gate access denied",
              "type" : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Autowired
  EndpointGateAspectMultipleGatesIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
