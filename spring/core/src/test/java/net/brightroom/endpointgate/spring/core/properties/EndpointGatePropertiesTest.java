package net.brightroom.endpointgate.spring.core.properties;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EndpointGatePropertiesTest {

  @Test
  void rolloutPercentages_excludesGatesWithoutRollout() {
    GateProperties withoutRollout = new GateProperties();

    var gates = new HashMap<String, GateProperties>();
    gates.put("no-rollout-gate", withoutRollout);

    EndpointGateProperties properties = new EndpointGateProperties();
    properties.setGates(gates);

    Map<String, Integer> result = properties.rolloutPercentages();

    assertTrue(result.isEmpty());
  }

  @Test
  void rolloutPercentages_includesGatesWithRollout() {
    GateProperties withRollout = new GateProperties();
    withRollout.setRollout(50);

    var gates = new HashMap<String, GateProperties>();
    gates.put("rollout-gate", withRollout);

    EndpointGateProperties properties = new EndpointGateProperties();
    properties.setGates(gates);

    Map<String, Integer> result = properties.rolloutPercentages();

    assertEquals(1, result.size());
    assertEquals(50, result.get("rollout-gate"));
  }

  // --- schedules() ---

  @Test
  void schedules_appliesDefaultTimezone_toGatesWithoutIndividualTimezone() {
    ScheduleProperties scheduleConfig = new ScheduleProperties();
    scheduleConfig.setStart(LocalDateTime.of(2026, 6, 1, 0, 0));

    GateProperties gate = new GateProperties();
    gate.setSchedule(scheduleConfig);

    var gates = new HashMap<String, GateProperties>();
    gates.put("gate-a", gate);

    GlobalScheduleProperties globalSchedule = new GlobalScheduleProperties();
    globalSchedule.setDefaultTimezone(ZoneId.of("Asia/Tokyo"));

    EndpointGateProperties properties = new EndpointGateProperties();
    properties.setGates(gates);
    properties.setSchedule(globalSchedule);

    var result = properties.schedules();

    assertEquals(ZoneId.of("Asia/Tokyo"), result.get("gate-a").timezone());
  }

  @Test
  void schedules_doesNotOverrideIndividualTimezone_withDefaultTimezone() {
    ScheduleProperties scheduleConfig = new ScheduleProperties();
    scheduleConfig.setStart(LocalDateTime.of(2026, 6, 1, 0, 0));
    scheduleConfig.setTimezone(ZoneId.of("Europe/London"));

    GateProperties gate = new GateProperties();
    gate.setSchedule(scheduleConfig);

    var gates = new HashMap<String, GateProperties>();
    gates.put("gate-a", gate);

    GlobalScheduleProperties globalSchedule = new GlobalScheduleProperties();
    globalSchedule.setDefaultTimezone(ZoneId.of("Asia/Tokyo"));

    EndpointGateProperties properties = new EndpointGateProperties();
    properties.setGates(gates);
    properties.setSchedule(globalSchedule);

    var result = properties.schedules();

    assertEquals(ZoneId.of("Europe/London"), result.get("gate-a").timezone());
  }

  @Test
  void schedules_leavesTimezoneNull_whenNoDefaultAndNoIndividualTimezone() {
    ScheduleProperties scheduleConfig = new ScheduleProperties();
    scheduleConfig.setStart(LocalDateTime.of(2026, 6, 1, 0, 0));

    GateProperties gate = new GateProperties();
    gate.setSchedule(scheduleConfig);

    var gates = new HashMap<String, GateProperties>();
    gates.put("gate-a", gate);

    EndpointGateProperties properties = new EndpointGateProperties();
    properties.setGates(gates);

    var result = properties.schedules();

    assertNull(result.get("gate-a").timezone());
  }

  @Test
  void rolloutPercentages_handlesMixedGates() {
    GateProperties withRollout = new GateProperties();
    withRollout.setRollout(75);

    GateProperties withoutRollout = new GateProperties();

    var gates = new HashMap<String, GateProperties>();
    gates.put("rollout-gate", withRollout);
    gates.put("no-rollout-gate", withoutRollout);

    EndpointGateProperties properties = new EndpointGateProperties();
    properties.setGates(gates);

    Map<String, Integer> result = properties.rolloutPercentages();

    assertEquals(1, result.size());
    assertEquals(75, result.get("rollout-gate"));
    assertFalse(result.containsKey("no-rollout-gate"));
  }
}
