package net.brightroom.endpointgate.spring.webmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateRouterConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(properties = {"endpoint-gate.response.type=HTML"})
@Import({EndpointGateMvcTestAutoConfiguration.class, EndpointGateRouterConfiguration.class})
class EndpointGateHandlerFilterFunctionHtmlResponseIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenNoFilter() throws Exception {
    mockMvc.perform(get("/functional/stable-endpoint")).andExpect(status().isOk());
  }

  @Test
  void shouldAllowAccess_whenGateIsEnabled() throws Exception {
    mockMvc.perform(get("/functional/experimental-stage-endpoint")).andExpect(status().isOk());
  }

  @Test
  void shouldBlockAccess_whenGateIsDisabled() throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(get("/functional/development-stage-endpoint"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();

    Document doc = Jsoup.parse(mvcResult.getResponse().getContentAsString());
    assertEquals("Access Denied", doc.title());
    assertEquals("403 - Access Denied", doc.select("h1").text());
    assertEquals("Gate 'development-stage-endpoint' is not available", doc.select("p").text());
  }

  @Test
  void shouldBlockAccess_whenGroupedRouteGateIsDisabled() throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(get("/functional/test/disable"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();

    Document doc = Jsoup.parse(mvcResult.getResponse().getContentAsString());
    assertEquals("Access Denied", doc.title());
    assertEquals("403 - Access Denied", doc.select("h1").text());
    assertEquals("Gate 'disable-class-level-feature' is not available", doc.select("p").text());
  }

  @Test
  void shouldAllowAccess_whenGroupedRouteGateIsEnabled() throws Exception {
    mockMvc.perform(get("/functional/test/enabled")).andExpect(status().isOk());
  }

  @Autowired
  EndpointGateHandlerFilterFunctionHtmlResponseIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
