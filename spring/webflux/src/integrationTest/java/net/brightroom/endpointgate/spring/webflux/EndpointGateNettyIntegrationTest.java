package net.brightroom.endpointgate.spring.webflux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class EndpointGateNettyIntegrationTest {

  @LocalServerPort int port;

  WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
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
    webTestClient.get().uri("/development-stage-endpoint").exchange().expectStatus().isForbidden();
  }

  @Test
  void shouldBlockAccess_whenFunctionalEndpointGateIsDisabled() {
    webTestClient
        .get()
        .uri("/functional/development-stage-endpoint")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void shouldAllowAccess_whenRemoteAddressConditionIsSatisfied() {
    // Real Netty server: remoteAddress resolves to 127.0.0.1 for localhost connections
    webTestClient
        .get()
        .uri("/condition/remote-address")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }
}
