package net.brightroom.endpointgate.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryScheduleProviderTest {

  @Test
  void getSchedule_returnsEmpty_whenGateNotPresent() {
    var provider = new InMemoryScheduleProvider(Map.of());

    assertTrue(provider.getSchedule("unknown").isEmpty());
  }

  @Test
  void getSchedule_returnsSchedule_whenGatePresent() {
    var schedule = new Schedule(LocalDateTime.of(2026, 1, 1, 0, 0), null, null);
    var provider = new InMemoryScheduleProvider(Map.of("my-gate", schedule));

    var result = provider.getSchedule("my-gate");

    assertTrue(result.isPresent());
    assertEquals(schedule, result.get());
  }

  @Test
  void getSchedule_returnsEmpty_forOtherGate_whenOnlyOneConfigured() {
    var schedule = new Schedule(null, LocalDateTime.of(2026, 12, 31, 23, 59), null);
    var provider = new InMemoryScheduleProvider(Map.of("gate-a", schedule));

    assertFalse(provider.getSchedule("gate-b").isPresent());
  }

  @Test
  void constructor_makesDefensiveCopy_soExternalMapChangesAreIgnored() {
    var schedule = new Schedule(LocalDateTime.of(2026, 1, 1, 0, 0), null, null);
    var mutableMap = new java.util.HashMap<String, Schedule>();
    mutableMap.put("gate-a", schedule);

    var provider = new InMemoryScheduleProvider(mutableMap);
    mutableMap.clear();

    // The provider still returns the schedule despite the external map being cleared
    assertTrue(provider.getSchedule("gate-a").isPresent());
  }
}
