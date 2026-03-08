package net.brightroom.endpointgate.spring.webflux;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateDisableController;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateEnableController;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateMethodLevelController;
import net.brightroom.endpointgate.spring.webflux.endpoint.NoEndpointGateController;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(
    properties = {"endpoint-gate.response.type=HTML"},
    controllers = {
      NoEndpointGateController.class,
      EndpointGateEnableController.class,
      EndpointGateDisableController.class,
      EndpointGateMethodLevelController.class,
    })
@Import(EndpointGateWebFluxTestAutoConfiguration.class)
class EndpointGateAspectHtmlResponseIntegrationTest {

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
    String html =
        webTestClient
            .get()
            .uri("/development-stage-endpoint")
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

    Document doc = Jsoup.parse(html);
    assertEquals("Access Denied", doc.title());
    assertEquals("403 - Access Denied", doc.select("h1").text());
    assertEquals("Gate 'development-stage-endpoint' is not available", doc.select("p").text());
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
    String html =
        webTestClient
            .get()
            .uri("/test/disable")
            .exchange()
            .expectStatus()
            .isForbidden()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

    Document doc = Jsoup.parse(html);
    assertEquals("Access Denied", doc.title());
    assertEquals("403 - Access Denied", doc.select("h1").text());
    assertEquals("Gate 'disable-class-level-feature' is not available", doc.select("p").text());
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
  EndpointGateAspectHtmlResponseIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
