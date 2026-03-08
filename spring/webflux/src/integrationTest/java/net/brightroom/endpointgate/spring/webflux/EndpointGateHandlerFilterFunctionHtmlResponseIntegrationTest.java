package net.brightroom.endpointgate.spring.webflux;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateRouterConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(properties = {"endpoint-gate.response.type=HTML"})
@Import({EndpointGateWebFluxTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionHtmlResponseIntegrationTest {

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
    String html =
        webTestClient
            .get()
            .uri("/functional/development-stage-endpoint")
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
  void shouldBlockAccess_whenClassLevelGateIsDisabled() {
    String html =
        webTestClient
            .get()
            .uri("/functional/test/disable")
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
        .uri("/functional/test/enabled")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Autowired
  EndpointGateHandlerFilterFunctionHtmlResponseIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
