package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateRouterConfiguration;
import net.brightroom.endpointgate.spring.webmvc.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.function.ServerResponse;

@WebMvcTest
@Import({
  EndpointGateHandlerFilterFunctionCustomResolutionIntegrationTest.CustomResolutionConfiguration
      .class,
  EndpointGateMvcTestAutoConfiguration.class,
  EndpointGateRouterConfiguration.class,
})
class EndpointGateHandlerFilterFunctionCustomResolutionIntegrationTest {

  @Configuration
  static class CustomResolutionConfiguration {

    @Bean
    @Primary
    AccessDeniedHandlerFilterResolution customResolution() {
      return (request, e) ->
          ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body("custom: " + e.gateId());
    }
  }

  MockMvc mockMvc;

  @Test
  void customResolutionTakesPriority_whenGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/functional/development-stage-endpoint"))
        .andExpect(status().is(HttpStatus.SERVICE_UNAVAILABLE.value()))
        .andExpect(content().string("custom: development-stage-endpoint"));
  }

  @Test
  void customResolutionTakesPriority_whenGroupedRouteGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/functional/test/disable"))
        .andExpect(status().is(HttpStatus.SERVICE_UNAVAILABLE.value()))
        .andExpect(content().string("custom: disable-class-level-feature"));
  }

  @Autowired
  EndpointGateHandlerFilterFunctionCustomResolutionIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
