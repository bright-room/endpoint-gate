package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateDisableController;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateEnableController;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateMethodLevelController;
import net.brightroom.endpointgate.spring.webmvc.endpoint.NoEndpointGateController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    properties = {"endpoint-gate.response.type=PLAIN_TEXT"},
    controllers = {
      NoEndpointGateController.class,
      EndpointGateEnableController.class,
      EndpointGateDisableController.class,
      EndpointGateMethodLevelController.class,
    })
@Import(EndpointGateMvcTestAutoConfiguration.class)
class EndpointGateInterceptorPlainTextResponseIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenNoAnnotated() throws Exception {
    mockMvc
        .perform(get("/stable-endpoint"))
        .andExpect(status().isOk())
        .andExpect(content().string("No Annotation"));
  }

  @Test
  void shouldAllowAccess_whenGateIsEnabled() throws Exception {
    mockMvc
        .perform(get("/experimental-stage-endpoint"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldBlockAccess_whenGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/development-stage-endpoint"))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Gate 'development-stage-endpoint' is not available"));
  }

  @Test
  void shouldAllowAccess_whenNoEndpointGateAnnotationOnController() throws Exception {
    mockMvc
        .perform(get("/test/no-annotation"))
        .andExpect(status().isOk())
        .andExpect(content().string("No Annotation"));
  }

  @Test
  void shouldBlockAccess_whenClassLevelGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/test/disable"))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Gate 'disable-class-level-feature' is not available"));
  }

  @Test
  void shouldAllowAccess_whenClassLevelGateIsEnabled() throws Exception {
    mockMvc
        .perform(get("/test/enabled"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Autowired
  EndpointGateInterceptorPlainTextResponseIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
