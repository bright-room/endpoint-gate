package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.spring.webflux.configuration.EndpointGateWebFluxTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webflux.endpoint.EndpointGateScheduleController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifies schedule-based endpoint gate control through the full WebFlux stack:
 *
 * <ul>
 *   <li>Property configuration → {@code InMemoryScheduleProvider} auto-wiring → aspect → HTTP
 *       response
 *   <li>Active schedule (start in the past) → 200 OK
 *   <li>Inactive schedule (start in the future) → 503 Service Unavailable + Retry-After header
 *   <li>Inactive schedule (end only, no start) → 503 Service Unavailable, no Retry-After header
 *   <li>Timezone-aware schedule → correctly evaluated in the configured timezone
 * </ul>
 */
@WebFluxTest(controllers = EndpointGateScheduleController.class)
@Import(EndpointGateWebFluxTestAutoConfiguration.class)
@TestPropertySource(
    properties = {
      // active-scheduled-gate: start far in the past → always active
      "endpoint-gate.gates.active-scheduled-gate.enabled=true",
      "endpoint-gate.gates.active-scheduled-gate.schedule.start=2020-01-01T00:00:00",
      // inactive-scheduled-gate: start far in the future → always inactive
      "endpoint-gate.gates.inactive-scheduled-gate.enabled=true",
      "endpoint-gate.gates.inactive-scheduled-gate.schedule.start=2099-01-01T00:00:00",
      // end-only-inactive-scheduled-gate: end in the past, no start → inactive, no retry-after
      "endpoint-gate.gates.end-only-inactive-scheduled-gate.enabled=true",
      "endpoint-gate.gates.end-only-inactive-scheduled-gate.schedule.end=2020-01-01T00:00:00",
      // timezone-scheduled-gate: start far in the past with timezone → always active
      "endpoint-gate.gates.timezone-scheduled-gate.enabled=true",
      "endpoint-gate.gates.timezone-scheduled-gate.schedule.start=2020-01-01T00:00:00",
      "endpoint-gate.gates.timezone-scheduled-gate.schedule.timezone=Asia/Tokyo",
    })
class EndpointGateAspectScheduleIntegrationTest {

  WebTestClient webTestClient;

  @Autowired
  EndpointGateAspectScheduleIntegrationTest(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }

  @Test
  void shouldAllowAccess_whenScheduleIsActive() {
    webTestClient
        .get()
        .uri("/schedule/active")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }

  @Test
  void shouldReturn503_whenScheduleIsInactive() {
    webTestClient
        .get()
        .uri("/schedule/inactive")
        .exchange()
        .expectStatus()
        .isEqualTo(503)
        .expectBody()
        .json(
            """
            {
              "status" : 503,
              "title" : "Endpoint gate temporarily unavailable",
              "type" : "https://github.com/bright-room/endpoint-gate#response-types"
            }
            """);
  }

  @Test
  void shouldReturnRetryAfterHeader_whenScheduleIsInactive() {
    webTestClient
        .get()
        .uri("/schedule/inactive")
        .exchange()
        .expectStatus()
        .isEqualTo(503)
        .expectHeader()
        .exists("Retry-After");
  }

  @Test
  void shouldReturn503WithoutRetryAfter_whenEndOnlyScheduleIsInactive() {
    webTestClient
        .get()
        .uri("/schedule/end-only-inactive")
        .exchange()
        .expectStatus()
        .isEqualTo(503)
        .expectHeader()
        .doesNotExist("Retry-After");
  }

  @Test
  void shouldAllowAccess_whenScheduleIsActiveWithTimezone() {
    webTestClient
        .get()
        .uri("/schedule/timezone")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("Allowed");
  }
}
