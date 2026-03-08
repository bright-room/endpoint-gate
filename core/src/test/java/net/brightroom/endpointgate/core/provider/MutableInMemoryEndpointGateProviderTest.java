package net.brightroom.endpointgate.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class MutableInMemoryEndpointGateProviderTest {

  @Test
  void isGateEnabled_returnsTrue_whenGateIsEnabled() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of("gate-a", true), false);

    assertTrue(provider.isGateEnabled("gate-a"));
  }

  @Test
  void isGateEnabled_returnsFalse_whenGateIsDisabled() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of("gate-a", false), true);

    assertFalse(provider.isGateEnabled("gate-a"));
  }

  @Test
  void isGateEnabled_returnsDefaultEnabled_false_whenGateIsUndefined() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of(), false);

    assertFalse(provider.isGateEnabled("undefined-gate"));
  }

  @Test
  void isGateEnabled_returnsDefaultEnabled_true_whenGateIsUndefined() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of(), true);

    assertTrue(provider.isGateEnabled("undefined-gate"));
  }

  @Test
  void getGates_returnsSnapshotOfAllGates() {
    var provider =
        new MutableInMemoryEndpointGateProvider(Map.of("gate-a", true, "gate-b", false), false);

    var gates = provider.getGates();

    assertEquals(Map.of("gate-a", true, "gate-b", false), gates);
  }

  @Test
  void getGates_returnsImmutableCopy_notAffectedBySubsequentMutations() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of("gate-a", true), false);
    var snapshot = provider.getGates();

    provider.setGateEnabled("gate-a", false);

    assertTrue(snapshot.get("gate-a"), "snapshot must not reflect subsequent mutations");
  }

  @Test
  void setGateEnabled_updatesExistingGate() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of("gate-a", true), false);

    provider.setGateEnabled("gate-a", false);

    assertFalse(provider.isGateEnabled("gate-a"));
  }

  @Test
  void setGateEnabled_addsNewGate() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of(), false);

    provider.setGateEnabled("new-gate", true);

    assertTrue(provider.isGateEnabled("new-gate"));
    assertEquals(Map.of("new-gate", true), provider.getGates());
  }

  @Test
  void concurrentWrites_doNotCorruptState() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of(), false);
    int threadCount = 100;
    List<String> gates = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      gates.add("gate-" + i);
    }

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (String gate : gates) {
        executor.submit(() -> provider.setGateEnabled(gate, true));
      }
    }

    assertEquals(threadCount, provider.getGates().size());
    gates.forEach(gate -> assertTrue(provider.isGateEnabled(gate)));
  }

  @Test
  void removeGate_removesExistingGate_andFallsBackToDefaultEnabled() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of("gate-a", true), false);

    provider.removeGate("gate-a");

    assertFalse(provider.isGateEnabled("gate-a"));
    assertTrue(provider.getGates().isEmpty());
  }

  @Test
  void removeGate_fallsBackToDefaultEnabled_true_whenDefaultEnabledIsTrue() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of("gate-a", false), true);

    provider.removeGate("gate-a");

    assertTrue(provider.isGateEnabled("gate-a"));
  }

  @Test
  void removeGate_isNoOp_whenGateDoesNotExist() {
    var provider = new MutableInMemoryEndpointGateProvider(Map.of("gate-a", true), false);

    provider.removeGate("nonexistent");

    assertEquals(Map.of("gate-a", true), provider.getGates());
  }
}
