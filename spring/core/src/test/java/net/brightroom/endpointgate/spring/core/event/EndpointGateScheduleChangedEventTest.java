package net.brightroom.endpointgate.spring.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import net.brightroom.endpointgate.core.provider.Schedule;
import org.junit.jupiter.api.Test;

class EndpointGateScheduleChangedEventTest {

  private static final Object SOURCE = new Object();

  @Test
  void constructor_withSchedule_setsFieldsCorrectly() {
    var schedule =
        new Schedule(
            LocalDateTime.of(2026, 12, 25, 0, 0),
            LocalDateTime.of(2027, 1, 5, 23, 59, 59),
            ZoneId.of("Asia/Tokyo"));

    var event = new EndpointGateScheduleChangedEvent(SOURCE, "gate-a", schedule);

    assertEquals("gate-a", event.gateId());
    assertEquals(schedule, event.schedule());
    assertEquals(SOURCE, event.getSource());
  }

  @Test
  void constructor_withNullSchedule_indicatesRemoval() {
    var event = new EndpointGateScheduleChangedEvent(SOURCE, "gate-a", null);

    assertEquals("gate-a", event.gateId());
    assertNull(event.schedule());
    assertEquals(SOURCE, event.getSource());
  }
}
