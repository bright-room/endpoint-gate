package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateRouterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(properties = {"endpoint-gate.response.type=PLAIN_TEXT"})
@Import({EndpointGateWebFluxTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionPlainTextResponseIntegrationTest {

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
        .expectBody(String.class)
        .isEqualTo("Gate 'development-stage-endpoint' is not available");
  }

  @Test
  void shouldBlockAccess_whenClassLevelGateIsDisabled() {
    webTestClient
        .get()
        .uri("/functional/test/disable")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody(String.class)
        .isEqualTo("Gate 'disable-class-level-feature' is not available");
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
  EndpointGateHandlerFilterFunctionPlainTextResponseIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
