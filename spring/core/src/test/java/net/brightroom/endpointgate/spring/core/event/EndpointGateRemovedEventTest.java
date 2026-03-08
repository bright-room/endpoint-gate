package net.brightroom.endpointgate.spring.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EndpointGateRemovedEventTest {

  @Test
  void constructor_setsGateIdAndSource() {
    Object source = new Object();

    var event = new EndpointGateRemovedEvent(source, "gate-a");

    assertEquals("gate-a", event.gateId());
    assertEquals(source, event.getSource());
  }
}
