package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateMultipleGatesController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {EndpointGateMultipleGatesController.class})
@Import(EndpointGateMvcTestAutoConfiguration.class)
class EndpointGateInterceptorMultipleGatesIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenAllGatesAreEnabled() throws Exception {
    mockMvc
        .perform(get("/multiple-gates/all-enabled"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldBlockAccess_whenSecondGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/multiple-gates/one-disabled"))
        .andExpect(status().isForbidden())
        .andExpect(
            content()
                .json(
                    """
                  {
                    "detail" : "Gate 'gate-disabled' is not available",
                    "instance" : "/multiple-gates/one-disabled",
                    "status" : 403,
                    "title" : "Endpoint gate access denied",
                    "type" : "https://github.com/bright-room/endpoint-gate#response-types"
                  }
                  """));
  }

  @Test
  void shouldBlockAccess_withFirstDisabledGateId_whenFirstGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/multiple-gates/first-disabled"))
        .andExpect(status().isForbidden())
        .andExpect(
            content()
                .json(
                    """
                  {
                    "detail" : "Gate 'gate-disabled' is not available",
                    "instance" : "/multiple-gates/first-disabled",
                    "status" : 403,
                    "title" : "Endpoint gate access denied",
                    "type" : "https://github.com/bright-room/endpoint-gate#response-types"
                  }
                  """));
  }

  @Autowired
  EndpointGateInterceptorMultipleGatesIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
