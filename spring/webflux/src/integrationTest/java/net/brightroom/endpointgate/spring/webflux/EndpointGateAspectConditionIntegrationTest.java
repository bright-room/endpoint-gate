package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateConditionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = EndpointGateConditionController.class)
@Import(EndpointGateWebFluxTestAutoConfiguration.class)
@TestPropertySource(
    properties = {
      "endpoint-gate.gates.header-condition-feature.enabled=true",
      "endpoint-gate.gates.header-condition-feature.condition=headers['X-Beta'] != null",
      "endpoint-gate.gates.param-condition-feature.enabled=true",
      "endpoint-gate.gates.param-condition-feature.condition=params['variant'] == 'B'",
      "endpoint-gate.gates.condition-rollout-feature.enabled=true",
      "endpoint-gate.gates.condition-rollout-feature.condition=headers['X-Beta'] != null",
      "endpoint-gate.gates.condition-rollout-feature.rollout=100",
      "endpoint-gate.gates.remote-address-condition-feature.enabled=true",
      "endpoint-gate.gates.remote-address-condition-feature.condition=remoteAddress == '127.0.0.1'",
    })
class EndpointGateAspectConditionIntegrationTest {

  WebTestClient webTestClient;

  @Test
  void shouldAllowAccess_whenHeaderConditionIsSatisfied() {
    webTestClient
        .get()
        .uri("/condition/header")
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
        .uri("/condition/header")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody()
        .json(
            """
            {
              "detail" : "Gate 'header-condition-feature' is not available",
              "instance" : "/condition/header",
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
        .uri("/condition/param?variant=B")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldBlockAccess_whenParamConditionIsNotSatisfied() {
    webTestClient.get().uri("/condition/param?variant=A").exchange().expectStatus().isForbidden();
  }

  @Test
  void shouldBlockAccess_whenParamIsMissing() {
    webTestClient.get().uri("/condition/param").exchange().expectStatus().isForbidden();
  }

  @Test
  void shouldBlockAccess_onConditionWithRollout_whenConditionIsNotSatisfied() {
    // condition fails (no X-Beta header) — access denied regardless of rollout
    webTestClient.get().uri("/condition/with-rollout").exchange().expectStatus().isForbidden();
  }

  @Test
  void shouldAllowAccess_onConditionWithRollout_whenConditionSatisfiedAndRolloutFull() {
    // rollout overridden to 100% via property, condition satisfied
    webTestClient
        .get()
        .uri("/condition/with-rollout")
        .header("X-Beta", "true")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldBlockAccess_whenRemoteAddressConditionIsNotSatisfied() {
    // @WebFluxTest slice does not set a real remote address, so the condition fails
    webTestClient.get().uri("/condition/remote-address").exchange().expectStatus().isForbidden();
  }

  @Autowired
  EndpointGateAspectConditionIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
