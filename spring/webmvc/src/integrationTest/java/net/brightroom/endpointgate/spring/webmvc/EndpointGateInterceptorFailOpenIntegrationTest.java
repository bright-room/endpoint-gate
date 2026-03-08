package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateUndefinedGateController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies fail-open behavior: when {@code endpoint-gate.default-enabled} is {@code true}, requests
 * to endpoints whose gate is absent from {@code endpoint-gate.gates} are allowed through.
 */
@WebMvcTest(
    properties = {"endpoint-gate.default-enabled=true"},
    controllers = EndpointGateUndefinedGateController.class)
@Import(EndpointGateMvcTestAutoConfiguration.class)
class EndpointGateInterceptorFailOpenIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenGateIsUndefinedInConfig_andDefaultEnabledIsTrue() throws Exception {
    mockMvc
        .perform(get("/undefined-gate-endpoint"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Autowired
  EndpointGateInterceptorFailOpenIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
