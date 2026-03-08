package net.brightroom.endpointgate.spring.webmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateMethodLevelController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = EndpointGateInterceptorRealServerIntegrationTest.TestConfig.class)
class EndpointGateInterceptorRealServerIntegrationTest {

  @Configuration
  @EnableAutoConfiguration
  @Import(EndpointGateMethodLevelController.class)
  static class TestConfig {}

  @Value("${local.server.port}")
  int port;

  @Test
  void shouldAllowAccess_whenGateIsEnabled() throws Exception {
    HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/experimental-stage-endpoint"))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals("Allowed", response.body());
  }

  @Test
  void shouldBlockAccess_whenGateIsDisabled() throws Exception {
    HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/development-stage-endpoint"))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString());

    assertEquals(403, response.statusCode());
    assertTrue(response.body().contains("Gate 'development-stage-endpoint' is not available"));
    assertTrue(response.body().contains("Endpoint gate access denied"));
  }
}
