package net.brightroom.endpointgate.spring.actuator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import net.brightroom.endpointgate.spring.actuator.configuration.EndpointGateActuatorTestAutoConfiguration;
import net.brightroom.endpointgate.spring.core.event.EndpointGateChangedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateRemovedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateScheduleChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = EndpointGateActuatorTestAutoConfiguration.class)
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "endpoint-gate.gates.gate-a.enabled=true",
      "endpoint-gate.gates.gate-a.rollout=50",
      "endpoint-gate.gates.gate-b.enabled=false",
      "endpoint-gate.default-enabled=false",
      "management.endpoints.web.exposure.include=endpoint-gates"
    })
@Import(EndpointGateEndpointIntegrationTest.EventCapture.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EndpointGateEndpointIntegrationTest {

  MockMvc mockMvc;
  EventCapture eventCapture;

  @BeforeEach
  void resetEvents() {
    eventCapture.clear();
  }

  @Test
  void get_returnsAllGatesAndDefaultEnabled() throws Exception {
    mockMvc
        .perform(get("/actuator/endpoint-gates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-a')].enabled", hasItem(true)))
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-b')].enabled", hasItem(false)))
        .andExpect(jsonPath("$.defaultEnabled").value(false));
  }

  @Test
  void post_updatesGateAndReturnsUpdatedState() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": false}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-a')].enabled", hasItem(false)));
  }

  @Test
  void post_thenGet_persistsUpdateInMemory() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-b", "enabled": true}
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/actuator/endpoint-gates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-b')].enabled", hasItem(true)));
  }

  @Test
  void post_addsNewGateNotPreviouslyDefined() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "new-gate", "enabled": true}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'new-gate')].enabled", hasItem(true)));
  }

  @Test
  void post_publishesEndpointGateChangedEvent() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": false}
                    """))
        .andExpect(status().isOk());

    assertEquals(1, eventCapture.events().size());
    var event = eventCapture.events().get(0);
    assertEquals("gate-a", event.gateId());
    assertEquals(false, event.enabled());
    assertNotNull(event.getSource());
  }

  @Test
  void get_withSelector_returnsIndividualGate() throws Exception {
    mockMvc
        .perform(get("/actuator/endpoint-gates/gate-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gateId").value("gate-a"))
        .andExpect(jsonPath("$.enabled").value(true));
  }

  @Test
  void get_withSelector_returnsDefaultEnabled_whenGateIsUndefined() throws Exception {
    mockMvc
        .perform(get("/actuator/endpoint-gates/undefined-gate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gateId").value("undefined-gate"))
        .andExpect(jsonPath("$.enabled").value(false));
  }

  @Test
  void post_thenGetWithSelector_reflectsUpdate() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-b", "enabled": true}
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/actuator/endpoint-gates/gate-b"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gateId").value("gate-b"))
        .andExpect(jsonPath("$.enabled").value(true));
  }

  @Test
  void get_returnsRolloutPercentageForEachGate() throws Exception {
    mockMvc
        .perform(get("/actuator/endpoint-gates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-a')].rollout", hasItem(50)))
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-b')].rollout", hasItem(100)));
  }

  @Test
  void get_withSelector_returnsRolloutPercentage() throws Exception {
    mockMvc
        .perform(get("/actuator/endpoint-gates/gate-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rollout").value(50));
  }

  @Test
  void post_withRollout_updatesRolloutPercentage() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "rollout": 80}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-a')].rollout", hasItem(80)));
  }

  @Test
  void post_withRollout_thenGet_persistsRolloutUpdate() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "rollout": 30}
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/actuator/endpoint-gates/gate-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rollout").value(30));
  }

  @Test
  void post_withRollout_publishesEventWithRolloutPercentage() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "rollout": 70}
                    """))
        .andExpect(status().isOk());

    assertEquals(1, eventCapture.events().size());
    var event = eventCapture.events().get(0);
    assertEquals("gate-a", event.gateId());
    assertEquals(true, event.enabled());
    assertEquals(70, event.rolloutPercentage());
  }

  @Test
  void delete_returnsNoContent() throws Exception {
    mockMvc.perform(delete("/actuator/endpoint-gates/gate-a")).andExpect(status().isNoContent());
  }

  @Test
  void delete_thenGet_gateIsRemoved() throws Exception {
    mockMvc.perform(delete("/actuator/endpoint-gates/gate-a")).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/actuator/endpoint-gates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-a')]").isEmpty());
  }

  @Test
  void delete_thenGetWithSelector_returnsDefaultEnabled() throws Exception {
    mockMvc.perform(delete("/actuator/endpoint-gates/gate-a")).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/actuator/endpoint-gates/gate-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gateId").value("gate-a"))
        .andExpect(jsonPath("$.enabled").value(false));
  }

  @Test
  void delete_isIdempotent_forNonexistentGate() throws Exception {
    mockMvc
        .perform(delete("/actuator/endpoint-gates/nonexistent"))
        .andExpect(status().isNoContent());
  }

  @Test
  void delete_publishesEndpointGateRemovedEvent() throws Exception {
    mockMvc.perform(delete("/actuator/endpoint-gates/gate-a")).andExpect(status().isNoContent());

    assertEquals(1, eventCapture.removedEvents().size());
    var event = eventCapture.removedEvents().get(0);
    assertEquals("gate-a", event.gateId());
    assertNotNull(event.getSource());
    assertTrue(
        eventCapture.events().isEmpty(), "DELETE should not publish EndpointGateChangedEvent");
  }

  @Test
  void delete_doesNotPublishRemovedEvent_forNonexistentGate() throws Exception {
    mockMvc
        .perform(delete("/actuator/endpoint-gates/nonexistent"))
        .andExpect(status().isNoContent());

    assertTrue(eventCapture.removedEvents().isEmpty());
  }

  @Autowired
  EndpointGateEndpointIntegrationTest(MockMvc mockMvc, EventCapture eventCapture) {
    this.mockMvc = mockMvc;
    this.eventCapture = eventCapture;
  }

  @Test
  void post_withSchedule_setsSchedule() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2026-04-01T00:00:00", "scheduleEnd": "2026-12-31T23:59:59", "scheduleTimezone": "Asia/Tokyo"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-a')].schedule.start").isNotEmpty());
  }

  @Test
  void post_withSchedule_thenGet_persistsSchedule() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2026-04-01T00:00:00"}
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/actuator/endpoint-gates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-a')].schedule.start").isNotEmpty());
  }

  @Test
  void post_withSchedule_thenGetWithSelector_returnsSchedule() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2026-04-01T00:00:00", "scheduleEnd": "2026-12-31T23:59:59", "scheduleTimezone": "Asia/Tokyo"}
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/actuator/endpoint-gates/gate-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.schedule.start").isNotEmpty())
        .andExpect(jsonPath("$.schedule.end").isNotEmpty())
        .andExpect(jsonPath("$.schedule.timezone").value("Asia/Tokyo"))
        .andExpect(jsonPath("$.schedule.active").isBoolean());
  }

  @Test
  void post_withSchedule_publishesScheduleChangedEvent() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2026-04-01T00:00:00"}
                    """))
        .andExpect(status().isOk());

    assertThat(eventCapture.scheduleChangedEvents()).hasSize(1);
    var event = eventCapture.scheduleChangedEvents().get(0);
    assertEquals("gate-a", event.gateId());
    assertNotNull(event.schedule());
  }

  @Test
  void post_withRemoveSchedule_removesExistingSchedule() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2026-04-01T00:00:00"}
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "removeSchedule": true}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gates[?(@.gateId == 'gate-a')].schedule.start").isEmpty());
  }

  @Test
  void post_withRemoveSchedule_publishesScheduleChangedEventWithNullSchedule() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2026-04-01T00:00:00"}
                    """))
        .andExpect(status().isOk());
    eventCapture.clear();

    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "removeSchedule": true}
                    """))
        .andExpect(status().isOk());

    assertThat(eventCapture.scheduleChangedEvents()).hasSize(1);
    assertNull(eventCapture.scheduleChangedEvents().get(0).schedule());
  }

  @Test
  void post_withSchedule_replacesExistingSchedule() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2026-04-01T00:00:00"}
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2027-01-01T00:00:00"}
                    """))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                "$.gates[?(@.gateId == 'gate-a')].schedule.start", hasItem("2027-01-01T00:00:00")));
  }

  @Test
  void delete_removesSchedule() throws Exception {
    mockMvc
        .perform(
            post("/actuator/endpoint-gates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"gateId": "gate-a", "enabled": true, "scheduleStart": "2026-04-01T00:00:00"}
                    """))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/actuator/endpoint-gates/gate-a")).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/actuator/endpoint-gates/gate-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.schedule").doesNotExist());
  }

  @Component
  static class EventCapture {

    private final List<EndpointGateChangedEvent> captured = new ArrayList<>();
    private final List<EndpointGateRemovedEvent> capturedRemoved = new ArrayList<>();
    private final List<EndpointGateScheduleChangedEvent> capturedScheduleChanged =
        new ArrayList<>();

    @EventListener
    void onEvent(EndpointGateChangedEvent event) {
      captured.add(event);
    }

    @EventListener
    void onEvent(EndpointGateRemovedEvent event) {
      capturedRemoved.add(event);
    }

    @EventListener
    void onEvent(EndpointGateScheduleChangedEvent event) {
      capturedScheduleChanged.add(event);
    }

    List<EndpointGateChangedEvent> events() {
      return List.copyOf(captured);
    }

    List<EndpointGateRemovedEvent> removedEvents() {
      return List.copyOf(capturedRemoved);
    }

    List<EndpointGateScheduleChangedEvent> scheduleChangedEvents() {
      return List.copyOf(capturedScheduleChanged);
    }

    void clear() {
      captured.clear();
      capturedRemoved.clear();
      capturedScheduleChanged.clear();
    }
  }
}
