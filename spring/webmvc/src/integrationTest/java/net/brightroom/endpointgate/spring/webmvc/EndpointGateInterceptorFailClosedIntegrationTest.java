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
 * Verifies fail-closed behavior: when {@code endpoint-gate.default-enabled} is {@code false},
 * requests to endpoints whose gate is absent from {@code endpoint-gate.gates} are blocked with
 * {@code 403 Forbidden}.
 */
@WebMvcTest(
    properties = {"endpoint-gate.default-enabled=false"},
    controllers = EndpointGateUndefinedGateController.class)
@Import(EndpointGateMvcTestAutoConfiguration.class)
class EndpointGateInterceptorFailClosedIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldBlockAccess_whenGateIsUndefinedInConfig_andDefaultEnabledIsFalse() throws Exception {
    mockMvc
        .perform(get("/undefined-gate-endpoint"))
        .andExpect(status().isForbidden())
        .andExpect(
            content()
                .json(
                    """
                    {
                      "detail"   : "Gate 'undefined-in-config-gate' is not available",
                      "instance" : "/undefined-gate-endpoint",
                      "status"   : 403,
                      "title"    : "Endpoint gate access denied",
                      "type"     : "https://github.com/bright-room/endpoint-gate#response-types"
                    }
                    """));
  }

  @Autowired
  EndpointGateInterceptorFailClosedIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
