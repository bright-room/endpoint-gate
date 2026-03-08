package net.brightroom.endpointgate.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MutableInMemoryRolloutPercentageProviderTest {

  @ParameterizedTest
  @ValueSource(ints = {0, 50, 100})
  void setRolloutPercentage_storesPercentage_whenValueIsValid(int percentage) {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of());

    provider.setRolloutPercentage("gate-a", percentage);

    assertEquals(OptionalInt.of(percentage), provider.getRolloutPercentage("gate-a"));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 101})
  void setRolloutPercentage_throwsIllegalArgumentException_whenValueIsOutOfRange(int percentage) {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of());

    assertThrows(
        IllegalArgumentException.class, () -> provider.setRolloutPercentage("gate-a", percentage));
  }

  @Test
  void getRolloutPercentage_returnsPercentage_whenExists() {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of("gate-a", 50));

    assertEquals(OptionalInt.of(50), provider.getRolloutPercentage("gate-a"));
  }

  @Test
  void getRolloutPercentage_returnsEmpty_whenNotExists() {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of());

    assertEquals(OptionalInt.empty(), provider.getRolloutPercentage("nonexistent"));
  }

  @Test
  void setRolloutPercentage_addsNewEntry() {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of());

    provider.setRolloutPercentage("gate-a", 75);

    assertEquals(OptionalInt.of(75), provider.getRolloutPercentage("gate-a"));
    assertEquals(Map.of("gate-a", 75), provider.getRolloutPercentages());
  }

  @Test
  void setRolloutPercentage_updatesExistingEntry() {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of("gate-a", 50));

    provider.setRolloutPercentage("gate-a", 80);

    assertEquals(OptionalInt.of(80), provider.getRolloutPercentage("gate-a"));
  }

  @Test
  void getRolloutPercentages_returnsSnapshotOfAllPercentages() {
    var provider =
        new MutableInMemoryRolloutPercentageProvider(Map.of("gate-a", 50, "gate-b", 100));

    var percentages = provider.getRolloutPercentages();

    assertEquals(Map.of("gate-a", 50, "gate-b", 100), percentages);
  }

  @Test
  void getRolloutPercentages_returnsImmutableCopy_notAffectedBySubsequentMutations() {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of("gate-a", 50));
    var snapshot = provider.getRolloutPercentages();

    provider.setRolloutPercentage("gate-a", 80);

    assertEquals(50, snapshot.get("gate-a"), "snapshot must not reflect subsequent mutations");
  }

  @Test
  void concurrentSetRolloutPercentage_doesNotCorruptState() {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of());
    int threadCount = 100;
    List<String> gates = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      gates.add("gate-" + i);
    }

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (String gate : gates) {
        executor.submit(() -> provider.setRolloutPercentage(gate, 50));
      }
    }

    assertEquals(threadCount, provider.getRolloutPercentages().size());
    gates.forEach(gate -> assertEquals(OptionalInt.of(50), provider.getRolloutPercentage(gate)));
  }

  @Test
  void removeRolloutPercentage_removesExistingEntry() {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of("gate-a", 50));

    provider.removeRolloutPercentage("gate-a");

    assertEquals(OptionalInt.empty(), provider.getRolloutPercentage("gate-a"));
    assertTrue(provider.getRolloutPercentages().isEmpty());
  }

  @Test
  void removeRolloutPercentage_isNoOp_whenEntryDoesNotExist() {
    var provider = new MutableInMemoryRolloutPercentageProvider(Map.of("gate-a", 50));

    provider.removeRolloutPercentage("nonexistent");

    assertEquals(OptionalInt.of(50), provider.getRolloutPercentage("gate-a"));
    assertEquals(Map.of("gate-a", 50), provider.getRolloutPercentages());
  }
}
