package net.brightroom.endpointgate.spring.actuator.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ReactiveEndpointGateHealthIndicatorTest {

  @Mock EndpointGateProperties properties;

  @Test
  void health_isUp_withMutableReactiveProvider() {
    var provider =
        new MutableInMemoryReactiveEndpointGateProvider(
            Map.of("gate-a", true, "gate-b", false), false);
    when(properties.defaultEnabled()).thenReturn(false);
    var indicator = new ReactiveEndpointGateHealthIndicator(provider, properties, null, List.of());

    Health health = indicator.health().block();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails())
        .containsEntry("provider", "MutableInMemoryReactiveEndpointGateProvider")
        .containsEntry("totalGates", 2L)
        .containsEntry("enabledGates", 1L)
        .containsEntry("disabledGates", 1L)
        .containsEntry("defaultEnabled", false);
  }

  @Test
  void health_isUp_withNonMutableReactiveProvider() {
    ReactiveEndpointGateProvider provider = gateId -> Mono.just("gate-a".equals(gateId));
    when(properties.gateIds()).thenReturn(Map.of("gate-a", true, "gate-b", false));
    when(properties.defaultEnabled()).thenReturn(false);
    var indicator = new ReactiveEndpointGateHealthIndicator(provider, properties, null, List.of());

    Health health = indicator.health().block();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails())
        .containsEntry("totalGates", 2L)
        .containsEntry("enabledGates", 1L)
        .containsEntry("disabledGates", 1L);
  }

  @Test
  void health_isDown_whenProviderThrowsException() {
    ReactiveEndpointGateProvider provider =
        gateId -> Mono.error(new RuntimeException("Connection refused"));
    when(properties.gateIds()).thenReturn(Map.of("gate-a", true));
    var indicator = new ReactiveEndpointGateHealthIndicator(provider, properties, null, List.of());

    Health health = indicator.health().block();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsKey("error");
  }

  @Test
  void health_isUp_withNoGates() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    when(properties.defaultEnabled()).thenReturn(false);
    var indicator = new ReactiveEndpointGateHealthIndicator(provider, properties, null, List.of());

    Health health = indicator.health().block();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails())
        .containsEntry("totalGates", 0L)
        .containsEntry("enabledGates", 0L)
        .containsEntry("disabledGates", 0L);
  }

  @Test
  void health_reflectsDefaultEnabled_true() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), true);
    when(properties.defaultEnabled()).thenReturn(true);
    var indicator = new ReactiveEndpointGateHealthIndicator(provider, properties, null, List.of());

    Health health = indicator.health().block();

    assertThat(health.getDetails()).containsEntry("defaultEnabled", true);
  }

  @Test
  void health_includesProviderClassName() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    when(properties.defaultEnabled()).thenReturn(false);
    var indicator = new ReactiveEndpointGateHealthIndicator(provider, properties, null, List.of());

    Health health = indicator.health().block();

    assertThat(health.getDetails())
        .containsEntry("provider", "MutableInMemoryReactiveEndpointGateProvider");
  }

  @Test
  void health_isDown_whenProviderExceedsTimeout() {
    ReactiveEndpointGateProvider provider =
        gateId -> Mono.just(true).delayElement(Duration.ofSeconds(5));
    when(properties.gateIds()).thenReturn(Map.of("gate-a", true));
    var indicator =
        new ReactiveEndpointGateHealthIndicator(
            provider, properties, Duration.ofMillis(100), List.of());

    Health health = indicator.health().block();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsKey("error");
  }

  @Test
  void health_isUp_whenProviderRespondsWithinTimeout() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    when(properties.defaultEnabled()).thenReturn(false);
    var indicator =
        new ReactiveEndpointGateHealthIndicator(
            provider, properties, Duration.ofSeconds(5), List.of());

    Health health = indicator.health().block();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
  }

  @Test
  void health_includesContributorDetails() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    when(properties.defaultEnabled()).thenReturn(false);
    ReactiveHealthDetailsContributor contributor =
        () -> Mono.just(Map.of("connectionPoolSize", 10, "latencyMs", 5));
    var indicator =
        new ReactiveEndpointGateHealthIndicator(provider, properties, null, List.of(contributor));

    Health health = indicator.health().block();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails())
        .containsEntry("connectionPoolSize", 10)
        .containsEntry("latencyMs", 5);
  }

  @Test
  void health_mergesMultipleContributorDetails() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    when(properties.defaultEnabled()).thenReturn(false);
    ReactiveHealthDetailsContributor contributor1 = () -> Mono.just(Map.of("key1", "value1"));
    ReactiveHealthDetailsContributor contributor2 = () -> Mono.just(Map.of("key2", "value2"));
    var indicator =
        new ReactiveEndpointGateHealthIndicator(
            provider, properties, null, List.of(contributor1, contributor2));

    Health health = indicator.health().block();

    assertThat(health.getDetails()).containsEntry("key1", "value1").containsEntry("key2", "value2");
  }

  @Test
  void health_withNoContributors_hasDefaultDetails() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    when(properties.defaultEnabled()).thenReturn(false);
    var indicator = new ReactiveEndpointGateHealthIndicator(provider, properties, null, List.of());

    Health health = indicator.health().block();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails())
        .containsKeys("provider", "totalGates", "enabledGates", "disabledGates", "defaultEnabled");
  }
}
