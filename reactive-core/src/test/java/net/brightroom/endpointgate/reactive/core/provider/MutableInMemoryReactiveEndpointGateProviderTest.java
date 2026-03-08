package net.brightroom.endpointgate.reactive.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class MutableInMemoryReactiveEndpointGateProviderTest {

  @Test
  void isGateEnabled_returnsTrue_whenGateIsEnabled() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);

    assertTrue(provider.isGateEnabled("gate-a").block());
  }

  @Test
  void isGateEnabled_returnsFalse_whenGateIsDisabled() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", false), true);

    assertFalse(provider.isGateEnabled("gate-a").block());
  }

  @Test
  void isGateEnabled_returnsDefaultEnabled_false_whenGateIsUndefined() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);

    assertFalse(provider.isGateEnabled("undefined-gate").block());
  }

  @Test
  void isGateEnabled_returnsDefaultEnabled_true_whenGateIsUndefined() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), true);

    assertTrue(provider.isGateEnabled("undefined-gate").block());
  }

  @Test
  void getGates_returnsSnapshotOfAllGates() {
    var provider =
        new MutableInMemoryReactiveEndpointGateProvider(
            Map.of("gate-a", true, "gate-b", false), false);

    assertEquals(Map.of("gate-a", true, "gate-b", false), provider.getGates().block());
  }

  @Test
  void getGates_returnsImmutableCopy_notAffectedBySubsequentMutations() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var snapshot = provider.getGates().block();

    provider.setGateEnabled("gate-a", false).block();

    assertTrue(snapshot.get("gate-a"), "snapshot must not reflect subsequent mutations");
  }

  @Test
  void setGateEnabled_updatesExistingGate() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);

    provider.setGateEnabled("gate-a", false).block();

    assertFalse(provider.isGateEnabled("gate-a").block());
  }

  @Test
  void setGateEnabled_addsNewGate() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);

    provider.setGateEnabled("new-gate", true).block();

    assertTrue(provider.isGateEnabled("new-gate").block());
    assertEquals(Map.of("new-gate", true), provider.getGates().block());
  }

  @Test
  void concurrentWrites_doNotCorruptState() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    int threadCount = 100;
    List<String> gates = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      gates.add("gate-" + i);
    }

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (String gate : gates) {
        executor.submit(() -> provider.setGateEnabled(gate, true).block());
      }
    }

    var gateMap = provider.getGates().block();
    assertEquals(threadCount, gateMap.size());
    gates.forEach(gate -> assertTrue(provider.isGateEnabled(gate).block()));
  }

  @Test
  void removeGate_removesExistingGate_andFallsBackToDefaultEnabled() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);

    provider.removeGate("gate-a").block();

    assertFalse(provider.isGateEnabled("gate-a").block());
    assertTrue(provider.getGates().block().isEmpty());
  }

  @Test
  void removeGate_fallsBackToDefaultEnabled_true_whenDefaultEnabledIsTrue() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", false), true);

    provider.removeGate("gate-a").block();

    assertTrue(provider.isGateEnabled("gate-a").block());
  }

  @Test
  void removeGate_isNoOp_whenGateDoesNotExist() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);

    provider.removeGate("nonexistent").block();

    assertEquals(Map.of("gate-a", true), provider.getGates().block());
  }
}
