package net.brightroom.endpointgate.spring.actuator;

import net.brightroom.endpointgate.spring.actuator.configuration.EndpointGateActuatorTestAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = EndpointGateActuatorTestAutoConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.main.web-application-type=reactive",
      "endpoint-gate.gates.gate-a.enabled=true",
      "endpoint-gate.gates.gate-b.enabled=false",
      "endpoint-gate.default-enabled=false",
      "management.endpoints.web.exposure.include=health",
      "management.endpoint.health.show-details=always"
    })
class ReactiveEndpointGateHealthIndicatorIntegrationTest {

  @LocalServerPort int port;

  WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void health_containsEndpointGateComponent() {
    webTestClient
        .get()
        .uri("/actuator/health")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("UP")
        .jsonPath("$.components.endpointGate.status")
        .isEqualTo("UP")
        .jsonPath("$.components.endpointGate.details.provider")
        .isEqualTo("MutableInMemoryReactiveEndpointGateProvider")
        .jsonPath("$.components.endpointGate.details.totalGates")
        .isEqualTo(2)
        .jsonPath("$.components.endpointGate.details.enabledGates")
        .isEqualTo(1)
        .jsonPath("$.components.endpointGate.details.disabledGates")
        .isEqualTo(1)
        .jsonPath("$.components.endpointGate.details.defaultEnabled")
        .isEqualTo(false);
  }
}
