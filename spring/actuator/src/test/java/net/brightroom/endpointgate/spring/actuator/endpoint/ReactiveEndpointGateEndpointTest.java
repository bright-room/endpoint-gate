package net.brightroom.endpointgate.spring.actuator.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.reactive.core.provider.InMemoryReactiveScheduleProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveEndpointGateProvider;
import net.brightroom.endpointgate.spring.core.event.EndpointGateChangedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateRemovedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ReactiveEndpointGateEndpointTest {

  @Mock ApplicationEventPublisher eventPublisher;

  private final Clock clock = Clock.systemDefaultZone();

  private MutableInMemoryReactiveRolloutPercentageProvider emptyRolloutProvider() {
    return new MutableInMemoryReactiveRolloutPercentageProvider(Map.of());
  }

  private MutableInMemoryReactiveConditionProvider emptyConditionProvider() {
    return new MutableInMemoryReactiveConditionProvider(Map.of());
  }

  private InMemoryReactiveScheduleProvider emptyScheduleProvider() {
    return new InMemoryReactiveScheduleProvider(Map.of());
  }

  private ReactiveEndpointGateEndpoint endpoint(
      MutableInMemoryReactiveEndpointGateProvider provider,
      MutableInMemoryReactiveRolloutPercentageProvider rolloutProvider,
      boolean defaultEnabled) {
    return new ReactiveEndpointGateEndpoint(
        provider,
        rolloutProvider,
        emptyConditionProvider(),
        emptyScheduleProvider(),
        defaultEnabled,
        eventPublisher,
        clock);
  }

  private ReactiveEndpointGateEndpoint endpointWithSchedule(
      MutableInMemoryReactiveEndpointGateProvider provider,
      Map<String, Schedule> schedules,
      boolean defaultEnabled) {
    return new ReactiveEndpointGateEndpoint(
        provider,
        emptyRolloutProvider(),
        emptyConditionProvider(),
        new InMemoryReactiveScheduleProvider(schedules),
        defaultEnabled,
        eventPublisher,
        clock);
  }

  @Test
  void gates_returnsAllGatesAndDefaultEnabled() {
    var provider =
        new MutableInMemoryReactiveEndpointGateProvider(
            Map.of("gate-a", true, "gate-b", false), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.gates();

    assertThat(response.gates())
        .extracting(EndpointGateEndpointResponse::gateId, EndpointGateEndpointResponse::enabled)
        .containsExactlyInAnyOrder(tuple("gate-a", true), tuple("gate-b", false));
    assertFalse(response.defaultEnabled());
  }

  @Test
  void updateGate_updatesExistingGateAndReturnsUpdatedState() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.updateGate("gate-a", false, null, null);

    assertThat(response.gates())
        .filteredOn(g -> g.gateId().equals("gate-a"))
        .extracting(EndpointGateEndpointResponse::enabled)
        .containsExactly(false);
  }

  @Test
  void updateGate_publishesEndpointGateChangedEvent() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    endpoint.updateGate("gate-a", false, null, null);

    var captor = ArgumentCaptor.forClass(EndpointGateChangedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertEquals("gate-a", captor.getValue().gateId());
    assertFalse(captor.getValue().enabled());
  }

  @Test
  void updateGate_addsNewGateNotPreviouslyDefined() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.updateGate("new-gate", true, null, null);

    assertThat(response.gates())
        .filteredOn(g -> g.gateId().equals("new-gate"))
        .extracting(EndpointGateEndpointResponse::enabled)
        .containsExactly(true);
  }

  @Test
  void gates_returnsDefaultEnabled_true_whenConfigured() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), true);
    var endpoint = endpoint(provider, emptyRolloutProvider(), true);

    var response = endpoint.gates();

    assertTrue(response.defaultEnabled());
  }

  @Test
  void updateGate_responseReflectsAllGatesIncludingUnchanged() {
    var provider =
        new MutableInMemoryReactiveEndpointGateProvider(
            Map.of("gate-a", true, "gate-b", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.updateGate("gate-a", false, null, null);

    assertEquals(2, response.gates().size());
    assertThat(response.gates())
        .extracting(EndpointGateEndpointResponse::gateId, EndpointGateEndpointResponse::enabled)
        .containsExactlyInAnyOrder(tuple("gate-a", false), tuple("gate-b", true));
  }

  @Test
  void updateGate_throwsIllegalArgumentException_whenGateIdIsNull() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> endpoint.updateGate(null, true, null, null))
        .withMessageContaining("gateId must not be null or blank");
  }

  @Test
  void updateGate_throwsIllegalArgumentException_whenGateIdIsEmpty() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> endpoint.updateGate("", true, null, null))
        .withMessageContaining("gateId must not be null or blank");
  }

  @Test
  void updateGate_throwsIllegalArgumentException_whenGateIdIsBlank() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> endpoint.updateGate("   ", true, null, null))
        .withMessageContaining("gateId must not be null or blank");
  }

  @Test
  void gate_returnsEnabledGate() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.gate("gate-a");

    assertEquals("gate-a", response.gateId());
    assertTrue(response.enabled());
  }

  @Test
  void gate_returnsDisabledGate() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", false), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.gate("gate-a");

    assertEquals("gate-a", response.gateId());
    assertFalse(response.enabled());
  }

  @Test
  void gate_returnsDefaultEnabled_whenGateIsUndefined() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.gate("undefined-gate");

    assertEquals("undefined-gate", response.gateId());
    assertFalse(response.enabled());
  }

  @Test
  void gate_returnsDefaultEnabled_true_whenGateIsUndefined_andDefaultEnabledIsTrue() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), true);
    var endpoint = endpoint(provider, emptyRolloutProvider(), true);

    var response = endpoint.gate("undefined-gate");

    assertEquals("undefined-gate", response.gateId());
    assertTrue(response.enabled());
  }

  @Test
  void gates_returnsEmptyList_whenProviderReturnsMonoEmpty() {
    var provider = mock(MutableReactiveEndpointGateProvider.class);
    when(provider.getGates()).thenReturn(Mono.empty());
    var endpoint =
        new ReactiveEndpointGateEndpoint(
            provider,
            emptyRolloutProvider(),
            emptyConditionProvider(),
            emptyScheduleProvider(),
            false,
            eventPublisher,
            clock);

    var response = endpoint.gates();

    assertThat(response.gates()).isEmpty();
    assertFalse(response.defaultEnabled());
  }

  @Test
  void gates_returnsRolloutPercentagesFromProvider() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var rolloutProvider =
        new MutableInMemoryReactiveRolloutPercentageProvider(Map.of("gate-a", 50));
    var endpoint = endpoint(provider, rolloutProvider, false);

    var response = endpoint.gates();

    assertThat(response.gates())
        .filteredOn(g -> g.gateId().equals("gate-a"))
        .extracting(EndpointGateEndpointResponse::rollout)
        .containsExactly(50);
  }

  @Test
  void gates_returnsDefaultRollout100_whenNotConfigured() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.gates();

    assertThat(response.gates())
        .filteredOn(g -> g.gateId().equals("gate-a"))
        .extracting(EndpointGateEndpointResponse::rollout)
        .containsExactly(100);
  }

  @Test
  void gate_returnsRolloutPercentageFromProvider() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var rolloutProvider =
        new MutableInMemoryReactiveRolloutPercentageProvider(Map.of("gate-a", 75));
    var endpoint = endpoint(provider, rolloutProvider, false);

    var response = endpoint.gate("gate-a");

    assertEquals(75, response.rollout());
  }

  @Test
  void gate_returnsDefaultRollout100_whenNotConfigured() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.gate("gate-a");

    assertEquals(100, response.rollout());
  }

  @Test
  void updateGate_updatesRolloutPercentage() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var rolloutProvider = new MutableInMemoryReactiveRolloutPercentageProvider(Map.of());
    var endpoint = endpoint(provider, rolloutProvider, false);

    var response = endpoint.updateGate("gate-a", true, 50, null);

    assertThat(response.gates())
        .filteredOn(g -> g.gateId().equals("gate-a"))
        .extracting(EndpointGateEndpointResponse::rollout)
        .containsExactly(50);
  }

  @Test
  void updateGate_publishesEventWithRolloutPercentage() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    endpoint.updateGate("gate-a", true, 60, null);

    var captor = ArgumentCaptor.forClass(EndpointGateChangedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertTrue(captor.getValue().enabled());
    assertEquals(60, captor.getValue().rolloutPercentage());
  }

  @Test
  void updateGate_publishesEventWithNullRollout_whenRolloutNotSpecified() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    endpoint.updateGate("gate-a", true, null, null);

    var captor = ArgumentCaptor.forClass(EndpointGateChangedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertNull(captor.getValue().rolloutPercentage());
  }

  @Test
  void updateGate_throwsIllegalArgumentException_whenRolloutIsNegative() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> endpoint.updateGate("gate-a", true, -1, null))
        .withMessageContaining("rollout must be between 0 and 100");
  }

  @Test
  void updateGate_throwsIllegalArgumentException_whenRolloutExceeds100() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> endpoint.updateGate("gate-a", true, 101, null))
        .withMessageContaining("rollout must be between 0 and 100");
  }

  @Test
  void updateGate_acceptsBoundaryRolloutValues() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var rolloutProvider = new MutableInMemoryReactiveRolloutPercentageProvider(Map.of());
    var endpoint = endpoint(provider, rolloutProvider, false);

    assertThatNoException().isThrownBy(() -> endpoint.updateGate("gate-a", true, 0, null));
    assertThatNoException().isThrownBy(() -> endpoint.updateGate("gate-a", true, 100, null));
  }

  @Test
  void deleteGate_removesGateFromProvider() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    endpoint.deleteGate("gate-a");

    assertFalse(provider.isGateEnabled("gate-a").block());
    assertTrue(provider.getGates().block().isEmpty());
  }

  @Test
  void deleteGate_publishesEndpointGateRemovedEvent() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    endpoint.deleteGate("gate-a");

    var captor = ArgumentCaptor.forClass(EndpointGateRemovedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertEquals("gate-a", captor.getValue().gateId());
  }

  @Test
  void deleteGate_removesRolloutPercentage() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var rolloutProvider =
        new MutableInMemoryReactiveRolloutPercentageProvider(Map.of("gate-a", 50));
    var endpoint = endpoint(provider, rolloutProvider, false);

    endpoint.deleteGate("gate-a");

    assertTrue(rolloutProvider.getRolloutPercentages().block().isEmpty());
  }

  @Test
  void deleteGate_thenGates_excludesDeletedGate() {
    var provider =
        new MutableInMemoryReactiveEndpointGateProvider(
            Map.of("gate-a", true, "gate-b", false), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    endpoint.deleteGate("gate-a");

    var response = endpoint.gates();
    assertThat(response.gates())
        .extracting(EndpointGateEndpointResponse::gateId)
        .containsExactly("gate-b");
  }

  @Test
  void deleteGate_isIdempotent_whenGateDoesNotExist() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatNoException().isThrownBy(() -> endpoint.deleteGate("nonexistent"));
    // 非存在ゲートの削除ではイベントが発行されない
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void deleteGate_throwsIllegalArgumentException_whenGateIdIsNull() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> endpoint.deleteGate(null))
        .withMessageContaining("gateId must not be null or blank");
  }

  @Test
  void deleteGate_throwsIllegalArgumentException_whenGateIdIsEmpty() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> endpoint.deleteGate(""))
        .withMessageContaining("gateId must not be null or blank");
  }

  @Test
  void deleteGate_throwsIllegalArgumentException_whenGateIdIsBlank() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of(), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> endpoint.deleteGate("   "))
        .withMessageContaining("gateId must not be null or blank");
  }

  // --- schedule response ---

  @Test
  void gate_returnsSchedule_whenScheduleIsConfigured() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    // active schedule: start in the past, no end
    var schedule = new Schedule(LocalDateTime.of(2020, 1, 1, 0, 0), null, null);
    var endpoint = endpointWithSchedule(provider, Map.of("gate-a", schedule), false);

    var response = endpoint.gate("gate-a");

    assertThat(response.schedule()).isNotNull();
    assertThat(response.schedule().start()).isEqualTo(LocalDateTime.of(2020, 1, 1, 0, 0));
    assertThat(response.schedule().end()).isNull();
    assertTrue(response.schedule().active());
  }

  @Test
  void gate_returnsNullSchedule_whenNoScheduleIsConfigured() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.gate("gate-a");

    assertNull(response.schedule());
  }

  @Test
  void gate_returnsInactiveSchedule_whenScheduleWindowHasPassed() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    // Inactive: end in the past
    var schedule =
        new Schedule(
            null,
            LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")).plusDays(1),
            ZoneId.of("UTC"));
    var fixedClock = Clock.fixed(Instant.now().plusSeconds(86400L * 365 * 100), ZoneId.of("UTC"));
    var endpointWithFixedClock =
        new ReactiveEndpointGateEndpoint(
            provider,
            emptyRolloutProvider(),
            emptyConditionProvider(),
            new InMemoryReactiveScheduleProvider(Map.of("gate-a", schedule)),
            false,
            eventPublisher,
            fixedClock);

    var response = endpointWithFixedClock.gate("gate-a");

    assertThat(response.schedule()).isNotNull();
    assertFalse(response.schedule().active());
  }

  @Test
  void gates_includesSchedule_whenScheduleIsConfigured() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var schedule = new Schedule(LocalDateTime.of(2020, 1, 1, 0, 0), null, null);
    var endpoint = endpointWithSchedule(provider, Map.of("gate-a", schedule), false);

    var response = endpoint.gates();

    assertThat(response.gates())
        .filteredOn(g -> g.gateId().equals("gate-a"))
        .extracting(EndpointGateEndpointResponse::schedule)
        .doesNotContainNull();
  }

  @Test
  void gates_hasNullSchedule_whenNoScheduleConfigured() {
    var provider = new MutableInMemoryReactiveEndpointGateProvider(Map.of("gate-a", true), false);
    var endpoint = endpoint(provider, emptyRolloutProvider(), false);

    var response = endpoint.gates();

    assertThat(response.gates())
        .filteredOn(g -> g.gateId().equals("gate-a"))
        .extracting(EndpointGateEndpointResponse::schedule)
        .containsOnlyNulls();
  }
}
