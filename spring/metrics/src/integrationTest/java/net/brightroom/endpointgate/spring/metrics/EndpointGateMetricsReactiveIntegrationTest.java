package net.brightroom.endpointgate.spring.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import net.brightroom.endpointgate.spring.metrics.configuration.EndpointGateMetricsWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.metrics.endpoint.ReactiveMetricsDisabledController;
import net.brightroom.endpointgate.spring.metrics.endpoint.ReactiveMetricsEnabledController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = EndpointGateMetricsWebFluxTestAutoConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.main.web-application-type=reactive"})
@Import({ReactiveMetricsEnabledController.class, ReactiveMetricsDisabledController.class})
@TestPropertySource(
    properties = {
      "endpoint-gate.gates.enabled-gate.enabled=true",
      "endpoint-gate.gates.disabled-gate.enabled=false",
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EndpointGateMetricsReactiveIntegrationTest {

  @LocalServerPort int port;

  MeterRegistry meterRegistry;
  WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void shouldRecordAllowedMetrics_whenGateIsEnabled() {
    webTestClient
        .get()
        .uri("/metrics-test/enabled")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");

    assertThat(
            meterRegistry
                .find("endpoint.gate.evaluations")
                .tag("gate.id", "enabled-gate")
                .tag("outcome", "allowed")
                .counter())
        .isNotNull()
        .satisfies(counter -> assertThat(counter.count()).isEqualTo(1.0));
  }

  @Test
  void shouldRecordDeniedMetrics_whenGateIsDisabled() {
    webTestClient.get().uri("/metrics-test/disabled").exchange().expectStatus().isForbidden();

    assertThat(
            meterRegistry
                .find("endpoint.gate.evaluations")
                .tag("gate.id", "disabled-gate")
                .tag("outcome", "denied.disabled")
                .counter())
        .isNotNull()
        .satisfies(counter -> assertThat(counter.count()).isEqualTo(1.0));
  }

  @Test
  void shouldRecordTimerMetrics() {
    webTestClient.get().uri("/metrics-test/enabled").exchange().expectStatus().isOk();

    assertThat(
            meterRegistry
                .find("endpoint.gate.evaluation.duration")
                .tag("gate.id", "enabled-gate")
                .tag("outcome", "allowed")
                .timer())
        .isNotNull()
        .satisfies(timer -> assertThat(timer.count()).isEqualTo(1));
  }

  @Autowired
  EndpointGateMetricsReactiveIntegrationTest(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }
}
