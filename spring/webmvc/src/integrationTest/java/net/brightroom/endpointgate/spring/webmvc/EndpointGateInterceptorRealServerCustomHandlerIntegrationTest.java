package net.brightroom.endpointgate.spring.webmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateDisableController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = EndpointGateInterceptorRealServerCustomHandlerIntegrationTest.TestConfig.class)
class EndpointGateInterceptorRealServerCustomHandlerIntegrationTest {

  @Configuration
  @EnableAutoConfiguration
  @Import({
    EndpointGateDisableController.class,
    EndpointGateInterceptorRealServerCustomHandlerIntegrationTest.CustomExceptionHandler.class
  })
  static class TestConfig {}

  @ControllerAdvice
  @Order(0)
  static class CustomExceptionHandler {

    @ExceptionHandler(EndpointGateAccessDeniedException.class)
    ResponseEntity<String> handle(EndpointGateAccessDeniedException e) {
      return ResponseEntity.status(503).body("custom: " + e.gateId());
    }
  }

  @Value("${local.server.port}")
  int port;

  @Test
  void customHandlerTakesPriority_whenGateIsDisabled() throws Exception {
    HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/test/disable"))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString());

    assertEquals(503, response.statusCode());
    assertEquals("custom: disable-class-level-feature", response.body());
  }
}
