package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateDisableController;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateEnableController;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateMethodLevelController;
import net.brightroom.endpointgate.spring.webflux.endpoint.NoEndpointGateController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(
    properties = {"endpoint-gate.response.type=PLAIN_TEXT"},
    controllers = {
      NoEndpointGateController.class,
      EndpointGateEnableController.class,
      EndpointGateDisableController.class,
      EndpointGateMethodLevelController.class,
    })
@Import(EndpointGateWebFluxTestAutoConfiguration.class)
class EndpointGateAspectPlainTextResponseIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldAllowAccess_whenNoAnnotated() {
    webTestClient
        .get()
        .uri("/stable-endpoint")
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
        .uri("/experimental-stage-endpoint")
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
        .uri("/development-stage-endpoint")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody(String.class)
        .isEqualTo("Gate 'development-stage-endpoint' is not available");
  }

  @Test
  void shouldAllowAccess_whenNoEndpointGateAnnotationOnController() {
    webTestClient
        .get()
        .uri("/test/no-annotation")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("No Annotation");
  }

  @Test
  void shouldBlockAccess_whenClassLevelGateIsDisabled() {
    webTestClient
        .get()
        .uri("/test/disable")
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
        .uri("/test/enabled")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldAllowAccess_whenMethodAnnotationOverridesClassAnnotation() {
    webTestClient
        .get()
        .uri("/test/method-override")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Method Override Allowed");
  }

  @Autowired
  EndpointGateAspectPlainTextResponseIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
