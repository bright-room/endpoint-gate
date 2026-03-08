package net.brightroom.endpointgate.spring.actuator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.brightroom.endpointgate.spring.actuator.configuration.EndpointGateActuatorTestAutoConfiguration;
import net.brightroom.endpointgate.spring.core.event.EndpointGateChangedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateRemovedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = EndpointGateActuatorTestAutoConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.main.web-application-type=reactive",
      "endpoint-gate.gates.gate-a.enabled=true",
      "endpoint-gate.gates.gate-a.rollout=50",
      "endpoint-gate.gates.gate-b.enabled=false",
      "endpoint-gate.default-enabled=false",
      "management.endpoints.web.exposure.include=endpoint-gates"
    })
@Import(ReactiveEndpointGateEndpointIntegrationTest.EventCapture.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SuppressWarnings("unchecked")
class ReactiveEndpointGateEndpointIntegrationTest {

  @LocalServerPort int port;

  WebTestClient webTestClient;
  EventCapture eventCapture;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    eventCapture.clear();
  }

  @Test
  void get_returnsAllGatesAndDefaultEnabled() {
    webTestClient
        .get()
        .uri("/actuator/endpoint-gates")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gates[?(@.gateId == 'gate-a')].enabled")
        .value(v -> assertThat((List<Object>) v).contains(true))
        .jsonPath("$.gates[?(@.gateId == 'gate-b')].enabled")
        .value(v -> assertThat((List<Object>) v).contains(false))
        .jsonPath("$.defaultEnabled")
        .isEqualTo(false);
  }

  @Test
  void post_updatesGateAndReturnsUpdatedState() {
    webTestClient
        .post()
        .uri("/actuator/endpoint-gates")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"gateId": "gate-a", "enabled": false}
            """)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gates[?(@.gateId == 'gate-a')].enabled")
        .value(v -> assertThat((List<Object>) v).contains(false));
  }

  @Test
  void post_thenGet_persistsUpdateInMemory() {
    webTestClient
        .post()
        .uri("/actuator/endpoint-gates")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"gateId": "gate-b", "enabled": true}
            """)
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .get()
        .uri("/actuator/endpoint-gates")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gates[?(@.gateId == 'gate-b')].enabled")
        .value(v -> assertThat((List<Object>) v).contains(true));
  }

  @Test
  void post_addsNewGateNotPreviouslyDefined() {
    webTestClient
        .post()
        .uri("/actuator/endpoint-gates")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"gateId": "new-gate", "enabled": true}
            """)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gates[?(@.gateId == 'new-gate')].enabled")
        .value(v -> assertThat((List<Object>) v).contains(true));
  }

  @Test
  void post_publishesEndpointGateChangedEvent() {
    webTestClient
        .post()
        .uri("/actuator/endpoint-gates")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"gateId": "gate-a", "enabled": false}
            """)
        .exchange()
        .expectStatus()
        .isOk();

    assertEquals(1, eventCapture.events().size());
    var event = eventCapture.events().get(0);
    assertEquals("gate-a", event.gateId());
    assertEquals(false, event.enabled());
    assertNotNull(event.getSource());
  }

  @Test
  void get_withSelector_returnsIndividualGate() {
    webTestClient
        .get()
        .uri("/actuator/endpoint-gates/gate-a")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gateId")
        .isEqualTo("gate-a")
        .jsonPath("$.enabled")
        .isEqualTo(true);
  }

  @Test
  void get_withSelector_returnsDefaultEnabled_whenGateIsUndefined() {
    webTestClient
        .get()
        .uri("/actuator/endpoint-gates/undefined-gate")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gateId")
        .isEqualTo("undefined-gate")
        .jsonPath("$.enabled")
        .isEqualTo(false);
  }

  @Test
  void post_thenGetWithSelector_reflectsUpdate() {
    webTestClient
        .post()
        .uri("/actuator/endpoint-gates")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"gateId": "gate-b", "enabled": true}
            """)
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .get()
        .uri("/actuator/endpoint-gates/gate-b")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gateId")
        .isEqualTo("gate-b")
        .jsonPath("$.enabled")
        .isEqualTo(true);
  }

  @Test
  void get_returnsRolloutPercentageForEachGate() {
    webTestClient
        .get()
        .uri("/actuator/endpoint-gates")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gates[?(@.gateId == 'gate-a')].rollout")
        .value(v -> assertThat((List<Object>) v).contains(50))
        .jsonPath("$.gates[?(@.gateId == 'gate-b')].rollout")
        .value(v -> assertThat((List<Object>) v).contains(100));
  }

  @Test
  void get_withSelector_returnsRolloutPercentage() {
    webTestClient
        .get()
        .uri("/actuator/endpoint-gates/gate-a")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.rollout")
        .isEqualTo(50);
  }

  @Test
  void post_withRollout_updatesRolloutPercentage() {
    webTestClient
        .post()
        .uri("/actuator/endpoint-gates")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"gateId": "gate-a", "enabled": true, "rollout": 80}
            """)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gates[?(@.gateId == 'gate-a')].rollout")
        .value(v -> assertThat((List<Object>) v).contains(80));
  }

  @Test
  void post_withRollout_thenGet_persistsRolloutUpdate() {
    webTestClient
        .post()
        .uri("/actuator/endpoint-gates")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"gateId": "gate-a", "enabled": true, "rollout": 30}
            """)
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .get()
        .uri("/actuator/endpoint-gates/gate-a")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.rollout")
        .isEqualTo(30);
  }

  @Test
  void post_withRollout_publishesEventWithRolloutPercentage() {
    webTestClient
        .post()
        .uri("/actuator/endpoint-gates")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {"gateId": "gate-a", "enabled": true, "rollout": 70}
            """)
        .exchange()
        .expectStatus()
        .isOk();

    assertEquals(1, eventCapture.events().size());
    var event = eventCapture.events().get(0);
    assertEquals("gate-a", event.gateId());
    assertEquals(true, event.enabled());
    assertEquals(70, event.rolloutPercentage());
  }

  @Test
  void delete_returnsNoContent() {
    webTestClient
        .delete()
        .uri("/actuator/endpoint-gates/gate-a")
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void delete_thenGet_gateIsRemoved() {
    webTestClient
        .delete()
        .uri("/actuator/endpoint-gates/gate-a")
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .get()
        .uri("/actuator/endpoint-gates")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gates[?(@.gateId == 'gate-a')]")
        .isEmpty();
  }

  @Test
  void delete_thenGetWithSelector_returnsDefaultEnabled() {
    webTestClient
        .delete()
        .uri("/actuator/endpoint-gates/gate-a")
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .get()
        .uri("/actuator/endpoint-gates/gate-a")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.gateId")
        .isEqualTo("gate-a")
        .jsonPath("$.enabled")
        .isEqualTo(false);
  }

  @Test
  void delete_isIdempotent_forNonexistentGate() {
    webTestClient
        .delete()
        .uri("/actuator/endpoint-gates/nonexistent")
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void delete_publishesEndpointGateRemovedEvent() {
    webTestClient
        .delete()
        .uri("/actuator/endpoint-gates/gate-a")
        .exchange()
        .expectStatus()
        .isNoContent();

    assertEquals(1, eventCapture.removedEvents().size());
    var event = eventCapture.removedEvents().get(0);
    assertEquals("gate-a", event.gateId());
    assertNotNull(event.getSource());
    assertTrue(
        eventCapture.events().isEmpty(), "DELETE should not publish EndpointGateChangedEvent");
  }

  @Test
  void delete_doesNotPublishRemovedEvent_forNonexistentGate() {
    webTestClient
        .delete()
        .uri("/actuator/endpoint-gates/nonexistent")
        .exchange()
        .expectStatus()
        .isNoContent();

    assertTrue(eventCapture.removedEvents().isEmpty());
  }

  @Autowired
  ReactiveEndpointGateEndpointIntegrationTest(EventCapture eventCapture) {
    this.eventCapture = eventCapture;
  }

  @Component
  static class EventCapture {

    private final List<EndpointGateChangedEvent> captured = new ArrayList<>();
    private final List<EndpointGateRemovedEvent> capturedRemoved = new ArrayList<>();

    @EventListener
    void onEvent(EndpointGateChangedEvent event) {
      captured.add(event);
    }

    @EventListener
    void onEvent(EndpointGateRemovedEvent event) {
      capturedRemoved.add(event);
    }

    List<EndpointGateChangedEvent> events() {
      return List.copyOf(captured);
    }

    List<EndpointGateRemovedEvent> removedEvents() {
      return List.copyOf(capturedRemoved);
    }

    void clear() {
      captured.clear();
      capturedRemoved.clear();
    }
  }
}
