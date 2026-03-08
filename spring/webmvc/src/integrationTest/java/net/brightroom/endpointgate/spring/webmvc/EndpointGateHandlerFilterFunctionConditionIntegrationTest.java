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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import({EndpointGateMvcTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
@TestPropertySource(
    properties = {
      "endpoint-gate.gates.conditional-gate.enabled=true",
    })
class EndpointGateHandlerFilterFunctionConditionIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenHeaderConditionIsSatisfied() throws Exception {
    mockMvc
        .perform(get("/functional/condition/header").header("X-Beta", "true"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldBlockAccess_whenHeaderConditionIsNotSatisfied() throws Exception {
    mockMvc
        .perform(get("/functional/condition/header"))
        .andExpect(status().isForbidden())
        .andExpect(
            content()
                .json(
                    """
                  {
                    "detail" : "Gate 'conditional-gate' is not available",
                    "instance" : "/functional/condition/header",
                    "status" : 403,
                    "title" : "Endpoint gate access denied",
                    "type" : "https://github.com/bright-room/endpoint-gate#response-types"
                  }
                  """));
  }

  @Autowired
  EndpointGateHandlerFilterFunctionConditionIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
