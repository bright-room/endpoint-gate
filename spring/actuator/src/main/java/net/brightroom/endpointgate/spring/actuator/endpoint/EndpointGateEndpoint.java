package net.brightroom.endpointgate.spring.actuator.endpoint;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import net.brightroom.endpointgate.core.provider.MutableConditionProvider;
import net.brightroom.endpointgate.core.provider.MutableEndpointGateProvider;
import net.brightroom.endpointgate.core.provider.MutableRolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.MutableScheduleProvider;
import net.brightroom.endpointgate.core.provider.Schedule;
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
 * Spring Boot Actuator endpoint for runtime endpoint gate management.
 *
 * <p>Exposed at {@code /actuator/endpoint-gates}. Allows reading and updating endpoint gate states
 * without restarting the application.
 *
 * <p>This endpoint is only registered when a {@link MutableEndpointGateProvider} bean is present in
 * the application context (see {@code EndpointGateActuatorAutoConfiguration}).
 *
 * <p>By default, both read and write operations are unrestricted. In production, consider
 * restricting access via {@code management.endpoint.endpoint-gates.access=READ_ONLY} or securing
 * the endpoint with Spring Security.
 */
@Endpoint(id = "endpoint-gates", defaultAccess = Access.UNRESTRICTED)
public class EndpointGateEndpoint {

  private final MutableEndpointGateProvider provider;
  private final MutableRolloutPercentageProvider rolloutProvider;
  private final MutableConditionProvider conditionProvider;
  private final MutableScheduleProvider scheduleProvider;
  private final boolean defaultEnabled;
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
    return new EndpointGateEndpointResponse(
        gateId,
        provider.isGateEnabled(gateId),
        rolloutProvider.getRolloutPercentage(gateId).orElse(100),
        conditionProvider.getCondition(gateId).orElse(null),
        buildScheduleResponse(gateId));
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
   * @param gateId the identifier of the endpoint gate to update
   * @param enabled the new enabled state
   * @param rollout the new rollout percentage (0–100), or {@code null} to leave unchanged
   * @param condition the new condition expression, {@code ""} to remove, or {@code null} to leave
   *     unchanged
   * @param scheduleStart the schedule start time, or {@code null} to leave unchanged
   * @param scheduleEnd the schedule end time, or {@code null} to leave unchanged
   * @param scheduleTimezone the schedule timezone string (e.g. {@code "Asia/Tokyo"}), or {@code
   *     null} to leave unchanged
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
    provider.setGateEnabled(gateId, enabled);
    if (rollout != null) {
      rolloutProvider.setRolloutPercentage(gateId, rollout);
    }
    if (condition != null) {
      if (condition.isEmpty()) {
        conditionProvider.removeCondition(gateId);
      } else {
        conditionProvider.setCondition(gateId, condition);
      }
    }
    if (Boolean.TRUE.equals(removeSchedule)) {
      scheduleProvider.removeSchedule(gateId);
      eventPublisher.publishEvent(new EndpointGateScheduleChangedEvent(this, gateId, null));
    } else if (scheduleStart != null || scheduleEnd != null || scheduleTimezone != null) {
      ZoneId timezone = null;
      if (scheduleTimezone != null && !scheduleTimezone.isEmpty()) {
        timezone = ZoneId.of(scheduleTimezone);
      }
      Schedule newSchedule = new Schedule(scheduleStart, scheduleEnd, timezone);
      scheduleProvider.setSchedule(gateId, newSchedule);
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
    boolean removed = provider.removeGate(gateId);
    rolloutProvider.removeRolloutPercentage(gateId);
    conditionProvider.removeCondition(gateId);
    scheduleProvider.removeSchedule(gateId);
    if (removed) {
      eventPublisher.publishEvent(new EndpointGateRemovedEvent(this, gateId));
    }
  }

  private EndpointGatesEndpointResponse buildGatesResponse() {
    var rolloutPercentages = rolloutProvider.getRolloutPercentages();
    var conditions = conditionProvider.getConditions();
    var schedules = scheduleProvider.getSchedules();
    var gateList =
        provider.getGates().entrySet().stream()
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
  private ScheduleEndpointResponse buildScheduleResponse(String gateId) {
    return scheduleProvider
        .getSchedule(gateId)
        .map(
            schedule ->
                new ScheduleEndpointResponse(
                    schedule.start(),
                    schedule.end(),
                    schedule.timezone(),
                    schedule.isActive(clock.instant())))
        .orElse(null);
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
   * Constructs an {@code EndpointGateEndpoint}.
   *
   * @param provider the mutable endpoint gate provider
   * @param rolloutProvider the mutable rollout percentage provider
   * @param conditionProvider the mutable condition provider
   * @param scheduleProvider the mutable schedule provider used to look up and mutate schedules per
   *     gate
   * @param defaultEnabled the default-enabled value to include in responses
   * @param eventPublisher the publisher used to broadcast gate change events
   * @param clock the clock used to determine schedule active status in responses
   */
  public EndpointGateEndpoint(
      MutableEndpointGateProvider provider,
      MutableRolloutPercentageProvider rolloutProvider,
      MutableConditionProvider conditionProvider,
      MutableScheduleProvider scheduleProvider,
      boolean defaultEnabled,
      ApplicationEventPublisher eventPublisher,
      Clock clock) {
    this.provider = provider;
    this.rolloutProvider = rolloutProvider;
    this.conditionProvider = conditionProvider;
    this.scheduleProvider = scheduleProvider;
    this.defaultEnabled = defaultEnabled;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }
}
