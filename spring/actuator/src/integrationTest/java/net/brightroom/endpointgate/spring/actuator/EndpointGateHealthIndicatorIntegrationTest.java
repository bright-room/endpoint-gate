package net.brightroom.endpointgate.spring.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.actuator.configuration.EndpointGateActuatorTestAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = EndpointGateActuatorTestAutoConfiguration.class)
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "endpoint-gate.gates.gate-a.enabled=true",
      "endpoint-gate.gates.gate-b.enabled=false",
      "endpoint-gate.default-enabled=false",
      "management.endpoints.web.exposure.include=health",
      "management.endpoint.health.show-details=always"
    })
class EndpointGateHealthIndicatorIntegrationTest {

  MockMvc mockMvc;

  @Autowired
  EndpointGateHealthIndicatorIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @Test
  void health_containsEndpointGateComponent() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"))
        .andExpect(jsonPath("$.components.endpointGate.status").value("UP"))
        .andExpect(
            jsonPath("$.components.endpointGate.details.provider")
                .value("MutableInMemoryEndpointGateProvider"))
        .andExpect(jsonPath("$.components.endpointGate.details.totalGates").value(2))
        .andExpect(jsonPath("$.components.endpointGate.details.enabledGates").value(1))
        .andExpect(jsonPath("$.components.endpointGate.details.disabledGates").value(1))
        .andExpect(jsonPath("$.components.endpointGate.details.defaultEnabled").value(false));
  }
}
