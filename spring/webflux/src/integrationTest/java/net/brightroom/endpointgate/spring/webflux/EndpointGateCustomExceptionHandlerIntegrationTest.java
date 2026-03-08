package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateDisableController;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateMethodLevelController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@WebFluxTest(
    controllers = {EndpointGateDisableController.class, EndpointGateMethodLevelController.class})
@Import({
  EndpointGateWebFluxTestAutoConfiguration.class,
  EndpointGateCustomExceptionHandlerIntegrationTest.CustomExceptionHandler.class
})
class EndpointGateCustomExceptionHandlerIntegrationTest {

  @ControllerAdvice
  @Order(0)
  static class CustomExceptionHandler {

    @ExceptionHandler(EndpointGateAccessDeniedException.class)
    ResponseEntity<String> handle(EndpointGateAccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("custom: " + e.gateId());
    }
  }

  WebTestClient webTestClient;

  @Test
  void customResolutionTakesPriority_whenClassLevelGateIsDisabled() {
    webTestClient
        .get()
        .uri("/test/disable")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
        .expectBody(String.class)
        .isEqualTo("custom: disable-class-level-feature");
  }

  @Test
  void customResolutionTakesPriority_whenMethodLevelGateIsDisabled() {
    webTestClient
        .get()
        .uri("/development-stage-endpoint")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
        .expectBody(String.class)
        .isEqualTo("custom: development-stage-endpoint");
  }

  @Autowired
  EndpointGateCustomExceptionHandlerIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }
}
