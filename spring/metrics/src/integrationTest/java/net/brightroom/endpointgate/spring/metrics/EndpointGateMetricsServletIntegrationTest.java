package net.brightroom.endpointgate.spring.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.micrometer.core.instrument.MeterRegistry;
import net.brightroom.endpointgate.spring.metrics.configuration.EndpointGateMetricsMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.metrics.endpoint.MetricsDisabledController;
import net.brightroom.endpointgate.spring.metrics.endpoint.MetricsEnabledController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = EndpointGateMetricsMvcTestAutoConfiguration.class)
@AutoConfigureMockMvc
@Import({MetricsEnabledController.class, MetricsDisabledController.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(
    properties = {
      "endpoint-gate.gates.enabled-gate.enabled=true",
      "endpoint-gate.gates.disabled-gate.enabled=false",
    })
class EndpointGateMetricsServletIntegrationTest {

  MockMvc mockMvc;
  MeterRegistry meterRegistry;

  @Test
  void shouldRecordAllowedMetrics_whenGateIsEnabled() throws Exception {
    mockMvc.perform(get("/metrics-test/enabled")).andExpect(status().isOk());

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
  void shouldRecordDeniedMetrics_whenGateIsDisabled() throws Exception {
    mockMvc.perform(get("/metrics-test/disabled")).andExpect(status().isForbidden());

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
  void shouldRecordTimerMetrics() throws Exception {
    mockMvc.perform(get("/metrics-test/enabled")).andExpect(status().isOk());

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
  EndpointGateMetricsServletIntegrationTest(MockMvc mockMvc, MeterRegistry meterRegistry) {
    this.mockMvc = mockMvc;
    this.meterRegistry = meterRegistry;
  }
}
