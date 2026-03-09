package net.brightroom.endpointgate.reactive.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.brightroom.endpointgate.core.provider.Schedule;
import org.junit.jupiter.api.Test;

class MutableInMemoryReactiveScheduleProviderTest {

  private static Schedule anySchedule() {
    return new Schedule(LocalDateTime.of(2025, 1, 1, 0, 0), null, null);
  }

  @Test
  void setSchedule_addsNewEntry() {
    var provider = new MutableInMemoryReactiveScheduleProvider(Map.of());
    var schedule = anySchedule();

    provider.setSchedule("gate-a", schedule).block();

    assertEquals(schedule, provider.getSchedule("gate-a").block());
    assertEquals(Map.of("gate-a", schedule), provider.getSchedules().block());
  }

  @Test
  void setSchedule_updatesExistingEntry() {
    var original = anySchedule();
    var provider = new MutableInMemoryReactiveScheduleProvider(Map.of("gate-a", original));
    var updated = new Schedule(LocalDateTime.of(2026, 6, 1, 0, 0), null, null);

    provider.setSchedule("gate-a", updated).block();

    assertEquals(updated, provider.getSchedule("gate-a").block());
  }

  @Test
  void getSchedule_returnsSchedule_whenExists() {
    var schedule = anySchedule();
    var provider = new MutableInMemoryReactiveScheduleProvider(Map.of("gate-a", schedule));

    assertEquals(schedule, provider.getSchedule("gate-a").block());
  }

  @Test
  void getSchedule_returnsEmpty_whenNotExists() {
    var provider = new MutableInMemoryReactiveScheduleProvider(Map.of());

    assertNull(provider.getSchedule("nonexistent").block());
  }

  @Test
  void getSchedules_returnsSnapshotOfAllSchedules() {
    var scheduleA = anySchedule();
    var scheduleB = new Schedule(null, LocalDateTime.of(2025, 12, 31, 23, 59), null);
    var provider =
        new MutableInMemoryReactiveScheduleProvider(
            Map.of("gate-a", scheduleA, "gate-b", scheduleB));

    var schedules = provider.getSchedules().block();

    assertEquals(2, schedules.size());
    assertEquals(scheduleA, schedules.get("gate-a"));
    assertEquals(scheduleB, schedules.get("gate-b"));
  }

  @Test
  void getSchedules_returnsImmutableCopy_notAffectedBySubsequentMutations() {
    var original = anySchedule();
    var provider = new MutableInMemoryReactiveScheduleProvider(Map.of("gate-a", original));
    var snapshot = provider.getSchedules().block();

    provider
        .setSchedule("gate-a", new Schedule(LocalDateTime.of(2030, 1, 1, 0, 0), null, null))
        .block();

    assertEquals(
        original, snapshot.get("gate-a"), "snapshot must not reflect subsequent mutations");
  }

  @Test
  void removeSchedule_removesExistingEntry() {
    var schedule = anySchedule();
    var provider = new MutableInMemoryReactiveScheduleProvider(Map.of("gate-a", schedule));

    Boolean removed = provider.removeSchedule("gate-a").block();

    assertTrue(removed);
    assertNull(provider.getSchedule("gate-a").block());
    assertTrue(provider.getSchedules().block().isEmpty());
  }

  @Test
  void removeSchedule_isNoOp_whenEntryDoesNotExist() {
    var schedule = anySchedule();
    var provider = new MutableInMemoryReactiveScheduleProvider(Map.of("gate-a", schedule));

    Boolean removed = provider.removeSchedule("nonexistent").block();

    assertFalse(removed);
    assertEquals(schedule, provider.getSchedule("gate-a").block());
    assertEquals(Map.of("gate-a", schedule), provider.getSchedules().block());
  }

  @Test
  void concurrentSetSchedule_doesNotCorruptState() {
    var provider = new MutableInMemoryReactiveScheduleProvider(Map.of());
    int threadCount = 100;
    List<String> gates = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      gates.add("gate-" + i);
    }
    var schedule = anySchedule();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (String gate : gates) {
        executor.submit(() -> provider.setSchedule(gate, schedule).block());
      }
    }

    assertEquals(threadCount, provider.getSchedules().block().size());
    gates.forEach(gate -> assertNotNull(provider.getSchedule(gate).block()));
  }
}
