package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateRouterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@Import({EndpointGateWebFluxTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
@TestPropertySource(
    properties = {
      "endpoint-gate.gates.conditional-feature.enabled=true",
    })
class EndpointGateHandlerFilterFunctionConditionIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldAllowAccess_whenHeaderConditionIsSatisfied() {
    webTestClient
        .get()
        .uri("/functional/condition/header")
        .header("X-Beta", "true")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldBlockAccess_whenHeaderConditionIsNotSatisfied() {
    webTestClient
        .get()
        .uri("/functional/condition/header")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail" : "Gate 'conditional-feature' is not available",
              "instance" : "/functional/condition/header",
              "status" : 403,
              "title" : "Endpoint gate access denied",
              "type" : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Test
  void shouldAllowAccess_whenParamConditionIsSatisfied() {
    webTestClient
        .get()
        .uri("/functional/condition/param?variant=B")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldBlockAccess_whenParamConditionIsNotSatisfied() {
    webTestClient
        .get()
        .uri("/functional/condition/param?variant=A")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Autowired
  EndpointGateHandlerFilterFunctionConditionIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
