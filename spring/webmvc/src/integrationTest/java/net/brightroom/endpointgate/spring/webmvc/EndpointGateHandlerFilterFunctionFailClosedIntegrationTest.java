package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateRouterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies fail-closed behavior for Functional Endpoints: when {@code
 * endpoint-gate.default-enabled} is {@code false}, requests to routes whose gate is absent from
 * {@code endpoint-gate.gates} are blocked with {@code 403 Forbidden}.
 */
@WebMvcTest(properties = {"endpoint-gate.default-enabled=false"})
@Import({EndpointGateMvcTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionFailClosedIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldBlockAccess_whenGateIsUndefinedInConfig_andDefaultEnabledIsFalse() throws Exception {
    mockMvc
        .perform(get("/functional/undefined-gate-endpoint"))
        .andExpect(status().isForbidden())
        .andExpect(
            content()
                .json(
                    """
                    {
                      "detail"   : "Gate 'undefined-in-config-gate' is not available",
                      "instance" : "/functional/undefined-gate-endpoint",
                      "status"   : 403,
                      "title"    : "Endpoint gate access denied",
                      "type"     : "https://github.com/bright-room/endpoint-gate#response-types"
                    }
                    """));
  }

  @Autowired
  EndpointGateHandlerFilterFunctionFailClosedIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
