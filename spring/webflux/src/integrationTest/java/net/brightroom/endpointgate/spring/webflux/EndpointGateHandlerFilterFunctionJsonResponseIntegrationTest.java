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
class EndpointGateHandlerFilterFunctionJsonResponseIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldAllowAccess_whenNoFilter() {
    webTestClient
        .get()
        .uri("/functional/stable-endpoint")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("No Annotation");
  }

  @Test
  void shouldAllowAccess_whenGateIsEnabled() {
    webTestClient
        .get()
        .uri("/functional/experimental-stage-endpoint")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldBlockAccess_whenGateIsDisabled() {
    webTestClient
        .get()
        .uri("/functional/development-stage-endpoint")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail" : "Gate 'development-stage-endpoint' is not available",
              "instance" : "/functional/development-stage-endpoint",
              "status" : 403,
              "title" : "Endpoint gate access denied",
              "type" : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Test
  void shouldBlockAccess_whenClassLevelGateIsDisabled() {
    webTestClient
        .get()
        .uri("/functional/test/disable")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail" : "Gate 'disable-class-level-feature' is not available",
              "instance" : "/functional/test/disable",
              "status" : 403,
              "title" : "Endpoint gate access denied",
              "type" : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Test
  void shouldAllowAccess_whenClassLevelGateIsEnabled() {
    webTestClient
        .get()
        .uri("/functional/test/enabled")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Autowired
  EndpointGateHandlerFilterFunctionJsonResponseIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
