package net.brightroom.endpointgate.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class MutableInMemoryScheduleProviderTest {

  private static Schedule anySchedule() {
    return new Schedule(LocalDateTime.of(2025, 1, 1, 0, 0), null, null);
  }

  @Test
  void setSchedule_addsNewEntry() {
    var provider = new MutableInMemoryScheduleProvider(Map.of());
    var schedule = anySchedule();

    provider.setSchedule("gate-a", schedule);

    assertEquals(Optional.of(schedule), provider.getSchedule("gate-a"));
    assertEquals(Map.of("gate-a", schedule), provider.getSchedules());
  }

  @Test
  void setSchedule_updatesExistingEntry() {
    var original = anySchedule();
    var provider = new MutableInMemoryScheduleProvider(Map.of("gate-a", original));
    var updated = new Schedule(LocalDateTime.of(2026, 6, 1, 0, 0), null, null);

    provider.setSchedule("gate-a", updated);

    assertEquals(Optional.of(updated), provider.getSchedule("gate-a"));
  }

  @Test
  void getSchedule_returnsSchedule_whenExists() {
    var schedule = anySchedule();
    var provider = new MutableInMemoryScheduleProvider(Map.of("gate-a", schedule));

    assertEquals(Optional.of(schedule), provider.getSchedule("gate-a"));
  }

  @Test
  void getSchedule_returnsEmpty_whenNotExists() {
    var provider = new MutableInMemoryScheduleProvider(Map.of());

    assertEquals(Optional.empty(), provider.getSchedule("nonexistent"));
  }

  @Test
  void getSchedules_returnsSnapshotOfAllSchedules() {
    var scheduleA = anySchedule();
    var scheduleB = new Schedule(null, LocalDateTime.of(2025, 12, 31, 23, 59), null);
    var provider =
        new MutableInMemoryScheduleProvider(Map.of("gate-a", scheduleA, "gate-b", scheduleB));

    var schedules = provider.getSchedules();

    assertEquals(2, schedules.size());
    assertEquals(scheduleA, schedules.get("gate-a"));
    assertEquals(scheduleB, schedules.get("gate-b"));
  }

  @Test
  void getSchedules_returnsImmutableCopy_notAffectedBySubsequentMutations() {
    var original = anySchedule();
    var provider = new MutableInMemoryScheduleProvider(Map.of("gate-a", original));
    var snapshot = provider.getSchedules();

    provider.setSchedule("gate-a", new Schedule(LocalDateTime.of(2030, 1, 1, 0, 0), null, null));

    assertEquals(
        original, snapshot.get("gate-a"), "snapshot must not reflect subsequent mutations");
  }

  @Test
  void removeSchedule_removesExistingEntry() {
    var schedule = anySchedule();
    var provider = new MutableInMemoryScheduleProvider(Map.of("gate-a", schedule));

    provider.removeSchedule("gate-a");

    assertEquals(Optional.empty(), provider.getSchedule("gate-a"));
    assertTrue(provider.getSchedules().isEmpty());
  }

  @Test
  void removeSchedule_isNoOp_whenEntryDoesNotExist() {
    var schedule = anySchedule();
    var provider = new MutableInMemoryScheduleProvider(Map.of("gate-a", schedule));

    provider.removeSchedule("nonexistent");

    assertEquals(Optional.of(schedule), provider.getSchedule("gate-a"));
    assertEquals(Map.of("gate-a", schedule), provider.getSchedules());
  }

  @Test
  void concurrentSetSchedule_doesNotCorruptState() {
    var provider = new MutableInMemoryScheduleProvider(Map.of());
    int threadCount = 100;
    List<String> gates = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      gates.add("gate-" + i);
    }
    var schedule = anySchedule();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (String gate : gates) {
        executor.submit(() -> provider.setSchedule(gate, schedule));
      }
    }

    assertEquals(threadCount, provider.getSchedules().size());
    gates.forEach(gate -> assertNotNull(provider.getSchedule(gate).orElse(null)));
  }
}
