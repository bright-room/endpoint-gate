package net.brightroom.endpointgate.spring.core.properties;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class SchedulePropertiesTest {

  private ScheduleProperties newSchedule() {
    return new ScheduleProperties();
  }

  // --- setEnd() validation ---

  @Test
  void setEnd_throwsIllegalArgumentException_whenEndIsBeforeStart() {
    ScheduleProperties schedule = newSchedule();
    schedule.setStart(LocalDateTime.of(2026, 6, 15, 12, 0));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> schedule.setEnd(LocalDateTime.of(2026, 6, 15, 11, 0)))
        .withMessageContaining("schedule.end must not be before schedule.start");
  }

  @Test
  void setEnd_doesNotThrow_whenEndEqualsStart() {
    ScheduleProperties schedule = newSchedule();
    schedule.setStart(LocalDateTime.of(2026, 6, 15, 12, 0));

    assertThatNoException().isThrownBy(() -> schedule.setEnd(LocalDateTime.of(2026, 6, 15, 12, 0)));
  }

  @Test
  void setEnd_doesNotThrow_whenEndIsAfterStart() {
    ScheduleProperties schedule = newSchedule();
    schedule.setStart(LocalDateTime.of(2026, 6, 15, 12, 0));

    assertThatNoException().isThrownBy(() -> schedule.setEnd(LocalDateTime.of(2026, 6, 15, 13, 0)));
  }

  @Test
  void setEnd_doesNotThrow_whenStartIsNull() {
    ScheduleProperties schedule = newSchedule();

    assertThatNoException().isThrownBy(() -> schedule.setEnd(LocalDateTime.of(2026, 6, 15, 11, 0)));
  }

  @Test
  void setEnd_doesNotThrow_whenEndIsNull() {
    ScheduleProperties schedule = newSchedule();
    schedule.setStart(LocalDateTime.of(2026, 6, 15, 12, 0));

    assertThatNoException().isThrownBy(() -> schedule.setEnd(null));
  }

  // --- setStart() validation ---

  @Test
  void setStart_throwsIllegalArgumentException_whenStartIsAfterEnd() {
    ScheduleProperties schedule = newSchedule();
    schedule.setEnd(LocalDateTime.of(2026, 6, 15, 12, 0));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> schedule.setStart(LocalDateTime.of(2026, 6, 15, 13, 0)))
        .withMessageContaining("schedule.start must not be after schedule.end");
  }

  @Test
  void setStart_doesNotThrow_whenStartEqualsEnd() {
    ScheduleProperties schedule = newSchedule();
    schedule.setEnd(LocalDateTime.of(2026, 6, 15, 12, 0));

    assertThatNoException()
        .isThrownBy(() -> schedule.setStart(LocalDateTime.of(2026, 6, 15, 12, 0)));
  }

  @Test
  void setStart_doesNotThrow_whenStartIsBeforeEnd() {
    ScheduleProperties schedule = newSchedule();
    schedule.setEnd(LocalDateTime.of(2026, 6, 15, 12, 0));

    assertThatNoException()
        .isThrownBy(() -> schedule.setStart(LocalDateTime.of(2026, 6, 15, 11, 0)));
  }

  @Test
  void setStart_doesNotThrow_whenEndIsNull() {
    ScheduleProperties schedule = newSchedule();

    assertThatNoException()
        .isThrownBy(() -> schedule.setStart(LocalDateTime.of(2026, 6, 15, 12, 0)));
  }

  @Test
  void setStart_doesNotThrow_whenStartIsNull() {
    ScheduleProperties schedule = newSchedule();
    schedule.setEnd(LocalDateTime.of(2026, 6, 15, 12, 0));

    assertThatNoException().isThrownBy(() -> schedule.setStart(null));
  }

  // --- toSchedule(ZoneId) ---

  @Test
  void toSchedule_usesIndividualTimezone_whenBothIndividualAndDefaultAreSet() {
    ScheduleProperties config = newSchedule();
    config.setStart(LocalDateTime.of(2026, 6, 15, 10, 0));
    config.setTimezone(ZoneId.of("Asia/Tokyo"));

    var schedule = config.toSchedule(ZoneId.of("Europe/London"));

    assertEquals(ZoneId.of("Asia/Tokyo"), schedule.timezone());
  }

  @Test
  void toSchedule_usesDefaultTimezone_whenIndividualTimezoneIsNull() {
    ScheduleProperties config = newSchedule();
    config.setStart(LocalDateTime.of(2026, 6, 15, 10, 0));

    var schedule = config.toSchedule(ZoneId.of("America/New_York"));

    assertEquals(ZoneId.of("America/New_York"), schedule.timezone());
  }

  @Test
  void toSchedule_returnsNullTimezone_whenBothIndividualAndDefaultAreNull() {
    ScheduleProperties config = newSchedule();
    config.setStart(LocalDateTime.of(2026, 6, 15, 10, 0));

    var schedule = config.toSchedule(null);

    assertNull(schedule.timezone());
  }

  @Test
  void toSchedule_returnsScheduleWithSameStartAndEnd() {
    ScheduleProperties config = newSchedule();
    config.setStart(LocalDateTime.of(2026, 6, 15, 10, 0));
    config.setEnd(LocalDateTime.of(2026, 6, 15, 18, 0));

    var schedule = config.toSchedule(null);

    assertEquals(LocalDateTime.of(2026, 6, 15, 10, 0), schedule.start());
    assertEquals(LocalDateTime.of(2026, 6, 15, 18, 0), schedule.end());
    assertNull(schedule.timezone());
  }
}
