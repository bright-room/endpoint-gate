package net.brightroom.endpointgate.spring.actuator.endpoint;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveScheduleProvider;
import net.brightroom.endpointgate.spring.core.event.EndpointGateChangedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateRemovedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateScheduleChangedEvent;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Spring Boot Actuator endpoint for runtime endpoint gate management in reactive applications.
 *
 * <p>Exposed at {@code /actuator/endpoint-gates}. Allows reading and updating endpoint gate states
 * without restarting the application.
 *
 * <p>This endpoint delegates to a {@link MutableReactiveEndpointGateProvider} and blocks on the
 * reactive operations. This is safe because actuator endpoints run on the management thread pool,
 * not on the event loop.
 *
 * <p>By default, both read and write operations are unrestricted. In production, consider
 * restricting access via {@code management.endpoint.endpoint-gates.access=READ_ONLY} or securing
 * the endpoint with Spring Security.
 */
@Endpoint(id = "endpoint-gates", defaultAccess = Access.UNRESTRICTED)
public class ReactiveEndpointGateEndpoint {

  private final MutableReactiveEndpointGateProvider provider;
  private final MutableReactiveRolloutPercentageProvider rolloutProvider;
  private final MutableReactiveConditionProvider conditionProvider;
  private final MutableReactiveScheduleProvider reactiveScheduleProvider;
  private final boolean defaultEnabled;
  @Nullable private final ZoneId defaultScheduleTimezone;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;

  /**
   * Returns the current state of all endpoint gates.
   *
   * @return a response containing all gates and the default-enabled policy
   */
  @ReadOperation
  public EndpointGatesEndpointResponse gates() {
    return buildGatesResponse();
  }

  /**
   * Returns the current state of a single endpoint gate.
   *
   * <p>If the gate is not defined, the response reflects the {@code defaultEnabled} policy.
   *
   * @param gateId the identifier of the endpoint gate to read
   * @return a response containing the gate identifier and its current enabled state
   */
  @ReadOperation
  public EndpointGateEndpointResponse gate(@Selector String gateId) {
    if (gateId == null || gateId.isBlank()) {
      throw new IllegalArgumentException("gateId must not be null or blank");
    }
    var enabled = provider.isGateEnabled(gateId).block();
    var rollout = rolloutProvider.getRolloutPercentage(gateId).blockOptional().orElse(100);
    var condition = conditionProvider.getCondition(gateId).blockOptional().orElse(null);
    var schedule = reactiveScheduleProvider.getSchedule(gateId).blockOptional().orElse(null);
    return new EndpointGateEndpointResponse(
        gateId, Boolean.TRUE.equals(enabled), rollout, condition, buildScheduleResponse(schedule));
  }

  /**
   * Updates the enabled state and optionally the rollout percentage and condition of an endpoint
   * gate, then publishes an {@link EndpointGateChangedEvent}.
   *
   * <p>If the gate does not exist, it is created with the given state.
   *
   * <p><b>Note:</b> {@link EndpointGateChangedEvent} is published on every invocation, regardless
   * of whether the value actually changed.
   *
   * <p>For the {@code condition} parameter:
   *
   * <ul>
   *   <li>{@code null} — condition is not changed
   *   <li>{@code ""} (empty string) — condition is removed
   *   <li>any other string — condition is set to the given value
   * </ul>
   *
   * <p>For schedule parameters: setting a schedule is a <b>full replacement</b>, not a partial
   * update. A new {@link Schedule} is created from all three schedule parameters, replacing any
   * existing schedule entirely. At least one of {@code scheduleStart} or {@code scheduleEnd} must
   * be provided; specifying only {@code scheduleTimezone} will result in an {@link
   * IllegalArgumentException}.
   *
   * @param gateId the identifier of the endpoint gate to update
   * @param enabled the new enabled state
   * @param rollout the new rollout percentage (0–100), or {@code null} to leave unchanged
   * @param condition the new condition expression, {@code ""} to remove, or {@code null} to leave
   *     unchanged
   * @param scheduleStart the schedule start time, or {@code null} if only an end time is needed
   * @param scheduleEnd the schedule end time, or {@code null} for an open-ended schedule
   * @param scheduleTimezone the schedule timezone string (e.g. {@code "Asia/Tokyo"}), or {@code
   *     null} to use the global default timezone ({@code endpoint-gate.schedule.default-timezone}),
   *     falling back to the system default timezone if no global default is configured
   * @param removeSchedule {@code true} to remove the schedule, or {@code null}/{@code false} to
   *     leave unchanged
   * @return a response reflecting the updated state of all gates
   */
  @WriteOperation
  public EndpointGatesEndpointResponse updateGate(
      String gateId,
      boolean enabled,
      @Nullable Integer rollout,
      @Nullable String condition,
      @Nullable LocalDateTime scheduleStart,
      @Nullable LocalDateTime scheduleEnd,
      @Nullable String scheduleTimezone,
      @Nullable Boolean removeSchedule) {
    if (gateId == null || gateId.isBlank()) {
      throw new IllegalArgumentException("gateId must not be null or blank");
    }
    if (rollout != null && (rollout < 0 || rollout > 100)) {
      throw new IllegalArgumentException("rollout must be between 0 and 100, but was: " + rollout);
    }
    provider.setGateEnabled(gateId, enabled).block();
    if (rollout != null) {
      rolloutProvider.setRolloutPercentage(gateId, rollout).block();
    }
    if (condition != null) {
      if (condition.isEmpty()) {
        conditionProvider.removeCondition(gateId).block();
      } else {
        conditionProvider.setCondition(gateId, condition).block();
      }
    }
    if (Boolean.TRUE.equals(removeSchedule)) {
      reactiveScheduleProvider.removeSchedule(gateId).block();
      eventPublisher.publishEvent(new EndpointGateScheduleChangedEvent(this, gateId, null));
    } else if (scheduleStart != null || scheduleEnd != null || scheduleTimezone != null) {
      if (scheduleStart == null && scheduleEnd == null) {
        throw new IllegalArgumentException(
            "At least one of scheduleStart or scheduleEnd is required when setting a schedule");
      }
      ZoneId timezone = defaultScheduleTimezone;
      if (scheduleTimezone != null && !scheduleTimezone.isEmpty()) {
        timezone = ZoneId.of(scheduleTimezone);
      }
      Schedule newSchedule = new Schedule(scheduleStart, scheduleEnd, timezone);
      reactiveScheduleProvider.setSchedule(gateId, newSchedule).block();
      eventPublisher.publishEvent(new EndpointGateScheduleChangedEvent(this, gateId, newSchedule));
    }
    eventPublisher.publishEvent(
        new EndpointGateChangedEvent(this, gateId, enabled, rollout, condition));
    return buildGatesResponse();
  }

  /**
   * Removes an endpoint gate and its associated rollout percentage and condition.
   *
   * <p>An {@link EndpointGateRemovedEvent} is published only if the gate actually existed. This
   * operation is idempotent: deleting a non-existent gate is a no-op and still returns 204 No
   * Content without publishing an event.
   *
   * @param gateId the identifier of the endpoint gate to remove
   * @throws IllegalArgumentException if {@code gateId} is {@code null} or blank
   */
  @DeleteOperation
  public void deleteGate(@Selector String gateId) {
    if (gateId == null || gateId.isBlank()) {
      throw new IllegalArgumentException("gateId must not be null or blank");
    }
    Boolean removed = provider.removeGate(gateId).block();
    rolloutProvider.removeRolloutPercentage(gateId).block();
    conditionProvider.removeCondition(gateId).block();
    reactiveScheduleProvider.removeSchedule(gateId).block();
    if (Boolean.TRUE.equals(removed)) {
      eventPublisher.publishEvent(new EndpointGateRemovedEvent(this, gateId));
    }
  }

  private EndpointGatesEndpointResponse buildGatesResponse() {
    var gates = provider.getGates().block();
    if (gates == null) {
      return new EndpointGatesEndpointResponse(List.of(), defaultEnabled);
    }
    var rolloutPercentages =
        rolloutProvider.getRolloutPercentages().blockOptional().orElse(Map.of());
    var conditions = conditionProvider.getConditions().blockOptional().orElse(Map.of());
    var schedules = reactiveScheduleProvider.getSchedules().blockOptional().orElse(Map.of());
    var gateList =
        gates.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(
                e ->
                    new EndpointGateEndpointResponse(
                        e.getKey(),
                        e.getValue(),
                        rolloutPercentages.getOrDefault(e.getKey(), 100),
                        conditions.getOrDefault(e.getKey(), null),
                        buildScheduleResponse(schedules.get(e.getKey()))))
            .toList();
    return new EndpointGatesEndpointResponse(gateList, defaultEnabled);
  }

  @Nullable
  private ScheduleEndpointResponse buildScheduleResponse(@Nullable Schedule schedule) {
    if (schedule == null) {
      return null;
    }
    return new ScheduleEndpointResponse(
        schedule.start(), schedule.end(), schedule.timezone(), schedule.isActive(clock.instant()));
  }

  /**
   * Constructs a {@code ReactiveEndpointGateEndpoint}.
   *
   * @param provider the mutable reactive endpoint gate provider
   * @param rolloutProvider the mutable reactive rollout percentage provider
   * @param conditionProvider the mutable reactive condition provider
   * @param reactiveScheduleProvider the mutable reactive schedule provider used to look up and
   *     mutate schedules per gate
   * @param defaultEnabled the default-enabled value to include in responses
   * @param defaultScheduleTimezone the global default timezone used when a schedule update does not
   *     specify a timezone, or {@code null} to use the system default timezone
   * @param eventPublisher the publisher used to broadcast gate change events
   * @param clock the clock used to determine schedule active status in responses
   */
  public ReactiveEndpointGateEndpoint(
      MutableReactiveEndpointGateProvider provider,
      MutableReactiveRolloutPercentageProvider rolloutProvider,
      MutableReactiveConditionProvider conditionProvider,
      MutableReactiveScheduleProvider reactiveScheduleProvider,
      boolean defaultEnabled,
      @Nullable ZoneId defaultScheduleTimezone,
      ApplicationEventPublisher eventPublisher,
      Clock clock) {
    this.provider = provider;
    this.rolloutProvider = rolloutProvider;
    this.conditionProvider = conditionProvider;
    this.reactiveScheduleProvider = reactiveScheduleProvider;
    this.defaultEnabled = defaultEnabled;
    this.defaultScheduleTimezone = defaultScheduleTimezone;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }
}
