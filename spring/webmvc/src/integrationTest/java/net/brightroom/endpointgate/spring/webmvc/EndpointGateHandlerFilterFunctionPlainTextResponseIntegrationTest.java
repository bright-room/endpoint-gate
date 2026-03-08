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

@WebMvcTest(properties = {"endpoint-gate.response.type=PLAIN_TEXT"})
@Import({EndpointGateMvcTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionPlainTextResponseIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenNoFilter() throws Exception {
    mockMvc
        .perform(get("/functional/stable-endpoint"))
        .andExpect(status().isOk())
        .andExpect(content().string("No Annotation"));
  }

  @Test
  void shouldAllowAccess_whenGateIsEnabled() throws Exception {
    mockMvc
        .perform(get("/functional/experimental-stage-endpoint"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldBlockAccess_whenGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/functional/development-stage-endpoint"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType("text/plain;charset=UTF-8"))
        .andExpect(content().string("Gate 'development-stage-endpoint' is not available"));
  }

  @Test
  void shouldBlockAccess_whenGroupedRouteGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/functional/test/disable"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType("text/plain;charset=UTF-8"))
        .andExpect(content().string("Gate 'disable-class-level-feature' is not available"));
  }

  @Test
  void shouldAllowAccess_whenGroupedRouteGateIsEnabled() throws Exception {
    mockMvc
        .perform(get("/functional/test/enabled"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Autowired
  EndpointGateHandlerFilterFunctionPlainTextResponseIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
