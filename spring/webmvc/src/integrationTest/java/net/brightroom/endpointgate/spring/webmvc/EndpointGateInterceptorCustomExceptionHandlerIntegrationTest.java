package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateDisableController;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateMethodLevelController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@WebMvcTest(
    controllers = {EndpointGateDisableController.class, EndpointGateMethodLevelController.class})
@Import({
  EndpointGateMvcTestAutoConfiguration.class,
  EndpointGateInterceptorCustomExceptionHandlerIntegrationTest.CustomExceptionHandler.class
})
class EndpointGateInterceptorCustomExceptionHandlerIntegrationTest {

  @ControllerAdvice
  @Order(0)
  static class CustomExceptionHandler {

    @ExceptionHandler(EndpointGateAccessDeniedException.class)
    ResponseEntity<String> handle(EndpointGateAccessDeniedException e) {
      return ResponseEntity.status(503).body("custom: " + e.gateId());
    }
  }

  MockMvc mockMvc;

  @Test
  void customHandlerTakesPriority_whenGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/test/disable"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(content().string("custom: disable-class-level-feature"));
  }

  @Test
  void customHandlerTakesPriority_whenMethodLevelGateIsDisabled() throws Exception {
    mockMvc
        .perform(get("/development-stage-endpoint"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(content().string("custom: development-stage-endpoint"));
  }

  @Autowired
  EndpointGateInterceptorCustomExceptionHandlerIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }
}
