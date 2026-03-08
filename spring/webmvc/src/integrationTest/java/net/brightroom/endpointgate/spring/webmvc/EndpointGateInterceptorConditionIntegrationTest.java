package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateConditionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EndpointGateConditionController.class)
@Import(EndpointGateMvcTestAutoConfiguration.class)
@TestPropertySource(
    properties = {
      "endpoint-gate.gates.header-condition-gate.enabled=true",
      "endpoint-gate.gates.header-condition-gate.condition=headers['X-Beta'] != null",
      "endpoint-gate.gates.param-condition-gate.enabled=true",
      "endpoint-gate.gates.param-condition-gate.condition=params['variant'] == 'B'",
      "endpoint-gate.gates.condition-rollout-gate.enabled=true",
      "endpoint-gate.gates.condition-rollout-gate.condition=headers['X-Beta'] != null",
      "endpoint-gate.gates.condition-rollout-gate.rollout=100",
      "endpoint-gate.gates.remote-address-condition-gate.enabled=true",
      "endpoint-gate.gates.remote-address-condition-gate.condition=remoteAddress == '127.0.0.1'",
    })
class EndpointGateInterceptorConditionIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenHeaderConditionIsSatisfied() throws Exception {
    mockMvc
        .perform(get("/condition/header").header("X-Beta", "true"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldBlockAccess_whenHeaderConditionIsNotSatisfied() throws Exception {
    mockMvc
        .perform(get("/condition/header"))
        .andExpect(status().isForbidden())
        .andExpect(
            content()
                .json(
                    """
                  {
                    "detail" : "Gate 'header-condition-gate' is not available",
                    "instance" : "/condition/header",
                    "status" : 403,
                    "title" : "Endpoint gate access denied",
                    "type" : "https://github.com/bright-room/endpoint-gate#response-types"
                  }
                  """));
  }

  @Test
  void shouldAllowAccess_whenParamConditionIsSatisfied() throws Exception {
    mockMvc
        .perform(get("/condition/param").param("variant", "B"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldBlockAccess_whenParamConditionIsNotSatisfied() throws Exception {
    mockMvc
        .perform(get("/condition/param").param("variant", "A"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldBlockAccess_whenParamIsMissing() throws Exception {
    mockMvc.perform(get("/condition/param")).andExpect(status().isForbidden());
  }

  @Test
  void shouldBlockAccess_onConditionWithRollout_whenConditionIsNotSatisfied() throws Exception {
    // condition fails (no X-Beta header) — access denied regardless of rollout
    mockMvc.perform(get("/condition/with-rollout")).andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowAccess_onConditionWithRollout_whenConditionSatisfiedAndRolloutFull()
      throws Exception {
    // rollout overridden to 100% via property, condition satisfied
    mockMvc
        .perform(get("/condition/with-rollout").header("X-Beta", "true"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldAllowAccess_whenRemoteAddressConditionIsSatisfied() throws Exception {
    // MockMvc defaults remoteAddr to 127.0.0.1
    mockMvc
        .perform(get("/condition/remote-address"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Autowired
  EndpointGateInterceptorConditionIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
