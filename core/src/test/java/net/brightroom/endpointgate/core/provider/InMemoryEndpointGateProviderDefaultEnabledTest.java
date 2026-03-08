package net.brightroom.endpointgate.core.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryEndpointGateProviderDefaultEnabledTest {

  @Test
  void isGateEnabled_returnsFalse_whenGateIsUndefined_andDefaultEnabledIsFalse() {
    var provider = new InMemoryEndpointGateProvider(Map.of("gate-a", true), false);
    assertFalse(provider.isGateEnabled("undefined-gate"));
  }

  @Test
  void isGateEnabled_returnsTrue_whenGateIsUndefined_andDefaultEnabledIsTrue() {
    var provider = new InMemoryEndpointGateProvider(Map.of("gate-a", true), true);
    assertTrue(provider.isGateEnabled("undefined-gate"));
  }

  @Test
  void isGateEnabled_returnsTrue_whenGateIsExplicitlyEnabled_regardlessOfDefaultEnabled() {
    var providerFailClosed = new InMemoryEndpointGateProvider(Map.of("gate-a", true), false);
    var providerFailOpen = new InMemoryEndpointGateProvider(Map.of("gate-a", true), true);

    assertTrue(providerFailClosed.isGateEnabled("gate-a"));
    assertTrue(providerFailOpen.isGateEnabled("gate-a"));
  }

  @Test
  void isGateEnabled_returnsFalse_whenGateIsExplicitlyDisabled_regardlessOfDefaultEnabled() {
    var providerFailClosed = new InMemoryEndpointGateProvider(Map.of("gate-a", false), false);
    var providerFailOpen = new InMemoryEndpointGateProvider(Map.of("gate-a", false), true);

    assertFalse(providerFailClosed.isGateEnabled("gate-a"));
    assertFalse(providerFailOpen.isGateEnabled("gate-a"));
  }
}
