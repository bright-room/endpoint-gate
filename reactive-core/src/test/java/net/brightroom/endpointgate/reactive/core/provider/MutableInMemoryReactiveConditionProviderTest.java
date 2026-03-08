package net.brightroom.endpointgate.reactive.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class MutableInMemoryReactiveConditionProviderTest {

  @Test
  void getCondition_returnsCondition_whenExists() {
    var provider =
        new MutableInMemoryReactiveConditionProvider(Map.of("gate-a", "headers['X-Beta'] != null"));

    assertEquals("headers['X-Beta'] != null", provider.getCondition("gate-a").block());
  }

  @Test
  void getCondition_returnsEmpty_whenNotExists() {
    var provider = new MutableInMemoryReactiveConditionProvider(Map.of());

    assertNull(provider.getCondition("nonexistent").block());
  }

  @Test
  void setCondition_addsNewEntry() {
    var provider = new MutableInMemoryReactiveConditionProvider(Map.of());

    provider.setCondition("gate-a", "params['debug'] != null").block();

    assertEquals("params['debug'] != null", provider.getCondition("gate-a").block());
    assertEquals(Map.of("gate-a", "params['debug'] != null"), provider.getConditions().block());
  }

  @Test
  void setCondition_updatesExistingEntry() {
    var provider =
        new MutableInMemoryReactiveConditionProvider(Map.of("gate-a", "headers['X-Beta'] != null"));

    provider.setCondition("gate-a", "params['debug'] != null").block();

    assertEquals("params['debug'] != null", provider.getCondition("gate-a").block());
  }

  @Test
  void getConditions_returnsSnapshotOfAllConditions() {
    var provider =
        new MutableInMemoryReactiveConditionProvider(
            Map.of("gate-a", "headers['X-Beta'] != null", "gate-b", "params['debug'] != null"));

    var conditions = provider.getConditions().block();

    assertEquals(
        Map.of("gate-a", "headers['X-Beta'] != null", "gate-b", "params['debug'] != null"),
        conditions);
  }

  @Test
  void getConditions_returnsImmutableCopy_notAffectedBySubsequentMutations() {
    var provider =
        new MutableInMemoryReactiveConditionProvider(Map.of("gate-a", "headers['X-Beta'] != null"));
    var snapshot = provider.getConditions().block();

    provider.setCondition("gate-a", "params['debug'] != null").block();

    assertEquals(
        "headers['X-Beta'] != null",
        snapshot.get("gate-a"),
        "snapshot must not reflect subsequent mutations");
  }

  @Test
  void removeCondition_removesExistingEntry() {
    var provider =
        new MutableInMemoryReactiveConditionProvider(Map.of("gate-a", "headers['X-Beta'] != null"));

    Boolean removed = provider.removeCondition("gate-a").block();

    assertTrue(removed);
    assertNull(provider.getCondition("gate-a").block());
    assertTrue(provider.getConditions().block().isEmpty());
  }

  @Test
  void removeCondition_isNoOp_whenEntryDoesNotExist() {
    var provider =
        new MutableInMemoryReactiveConditionProvider(Map.of("gate-a", "headers['X-Beta'] != null"));

    Boolean removed = provider.removeCondition("nonexistent").block();

    assertFalse(removed);
    assertEquals("headers['X-Beta'] != null", provider.getCondition("gate-a").block());
    assertEquals(Map.of("gate-a", "headers['X-Beta'] != null"), provider.getConditions().block());
  }

  @Test
  void concurrentSetCondition_doesNotCorruptState() {
    var provider = new MutableInMemoryReactiveConditionProvider(Map.of());
    int threadCount = 100;
    List<String> gates = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      gates.add("gate-" + i);
    }

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (String gate : gates) {
        executor.submit(() -> provider.setCondition(gate, "headers['X-Beta'] != null").block());
      }
    }

    assertEquals(threadCount, provider.getConditions().block().size());
    gates.forEach(
        gate -> assertEquals("headers['X-Beta'] != null", provider.getCondition(gate).block()));
  }
}
