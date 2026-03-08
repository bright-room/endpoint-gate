package net.brightroom.endpointgate.spring.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EndpointGateChangedEventTest {

  private static final Object SOURCE = new Object();

  @Test
  void constructor_withEnabledOnly_setsFieldsCorrectly() {
    var event = new EndpointGateChangedEvent(SOURCE, "gate-a", true);

    assertEquals("gate-a", event.gateId());
    assertTrue(event.enabled());
    assertNull(event.rolloutPercentage());
    assertNull(event.condition());
    assertEquals(SOURCE, event.getSource());
  }

  @Test
  void constructor_withEnabledAndRollout_setsFieldsCorrectly() {
    var event = new EndpointGateChangedEvent(SOURCE, "gate-a", false, 50);

    assertEquals("gate-a", event.gateId());
    assertEquals(false, event.enabled());
    assertEquals(50, event.rolloutPercentage());
    assertNull(event.condition());
  }

  @Test
  void constructor_withAllFields_setsFieldsCorrectly() {
    var event =
        new EndpointGateChangedEvent(SOURCE, "gate-a", true, 75, "headers['X-Beta'] != null");

    assertEquals("gate-a", event.gateId());
    assertTrue(event.enabled());
    assertEquals(75, event.rolloutPercentage());
    assertEquals("headers['X-Beta'] != null", event.condition());
  }

  @Test
  void constructor_withNullRolloutAndCondition_returnsNull() {
    var event = new EndpointGateChangedEvent(SOURCE, "gate-a", true, null, null);

    assertNull(event.rolloutPercentage());
    assertNull(event.condition());
  }
}
