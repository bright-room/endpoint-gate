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

@WebMvcTest
@Import({EndpointGateMvcTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionMultipleGatesIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenAllGatesAreEnabled() throws Exception {
    mockMvc
        .perform(get("/functional/multiple-gates/all-enabled"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldBlockAccess_whenOneGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/functional/multiple-gates/one-disabled"))
        .andExpect(status().isForbidden())
        .andExpect(
            content()
                .json(
                    """
                  {
                    "detail" : "Gate 'gate-disabled' is not available",
                    "instance" : "/functional/multiple-gates/one-disabled",
                    "status" : 403,
                    "title" : "Endpoint gate access denied",
                    "type" : "https://github.com/bright-room/endpoint-gate#response-types"
                  }
                  """));
  }

  @Autowired
  EndpointGateHandlerFilterFunctionMultipleGatesIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
