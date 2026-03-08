package net.brightroom.endpointgate.spring.webmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateDisableViewController;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateEnableViewController;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateMethodLevelViewController;
import net.brightroom.endpointgate.spring.webmvc.endpoint.NoEndpointGateViewController;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(
    properties = {"endpoint-gate.response.type=HTML"},
    controllers = {
      NoEndpointGateViewController.class,
      EndpointGateEnableViewController.class,
      EndpointGateDisableViewController.class,
      EndpointGateMethodLevelViewController.class,
    })
@Import(EndpointGateMvcTestAutoConfiguration.class)
class EndpointGateInterceptorHtmlResponseIntegrationTest {

  MockMvc mockMvc;

  @Test
  void shouldAllowAccess_whenNoAnnotated() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/stable")).andExpect(status().isOk()).andReturn();

    MockHttpServletResponse response = mvcResult.getResponse();
    String htmlContent = response.getContentAsString();

    Document doc = Jsoup.parse(htmlContent);

    assertEquals("Stable page", doc.title());

    Elements h1Elements = doc.select("h1");
    assertEquals(1, h1Elements.size());
    assertEquals("stable-page", h1Elements.text());
  }

  @Test
  void shouldAllowAccess_whenGateIsEnabled() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/experimental-stage")).andExpect(status().isOk()).andReturn();

    MockHttpServletResponse response = mvcResult.getResponse();
    String htmlContent = response.getContentAsString();

    Document doc = Jsoup.parse(htmlContent);

    assertEquals("Experimental stage page", doc.title());

    Elements h1Elements = doc.select("h1");
    assertEquals(1, h1Elements.size());
    assertEquals("experimental-stage", h1Elements.text());
  }

  @Test
  void shouldBlockAccess_whenGateIsDisabled() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/development-stage")).andExpect(status().isForbidden()).andReturn();

    String htmlContent = mvcResult.getResponse().getContentAsString();
    Document doc = Jsoup.parse(htmlContent);

    assertEquals("Access Denied", doc.title());
    assertEquals("403 - Access Denied", doc.select("h1").text());
    assertEquals("Gate 'development-stage-endpoint' is not available", doc.select("p").text());
  }

  @Test
  void shouldAllowAccess_whenNoEndpointGateAnnotationOnController() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/view/test/no-annotation")).andExpect(status().isOk()).andReturn();

    MockHttpServletResponse response = mvcResult.getResponse();
    String htmlContent = response.getContentAsString();

    Document doc = Jsoup.parse(htmlContent);

    assertEquals("No annotation page", doc.title());

    Elements h1Elements = doc.select("h1");
    assertEquals(1, h1Elements.size());
    assertEquals("no-annotation", h1Elements.text());
  }

  @Test
  void shouldBlockAccess_whenClassLevelGateIsDisabled() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/view/test/disable")).andExpect(status().isForbidden()).andReturn();

    String htmlContent = mvcResult.getResponse().getContentAsString();
    Document doc = Jsoup.parse(htmlContent);

    assertEquals("Access Denied", doc.title());
    assertEquals("403 - Access Denied", doc.select("h1").text());
    assertEquals("Gate 'disable-class-level-feature' is not available", doc.select("p").text());
  }

  @Test
  void shouldAllowAccess_whenNoEndpointGateAnnotation() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/view/test/enabled")).andExpect(status().isOk()).andReturn();

    MockHttpServletResponse response = mvcResult.getResponse();
    String htmlContent = response.getContentAsString();

    Document doc = Jsoup.parse(htmlContent);

    assertEquals("Enable page", doc.title());

    Elements h1Elements = doc.select("h1");
    assertEquals(1, h1Elements.size());
    assertEquals("enable", h1Elements.text());
  }

  @Autowired
  EndpointGateInterceptorHtmlResponseIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
