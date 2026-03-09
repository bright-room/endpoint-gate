package net.brightroom.endpointgate.spring.core.event;

import net.brightroom.endpointgate.core.provider.Schedule;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when the schedule of an endpoint gate is set, updated, or removed at runtime.
 *
 * <p>Listeners can subscribe to this event via {@code @EventListener} to react to schedule changes
 * exclusively, without receiving unrelated gate state changes.
 *
 * <p>When {@link #schedule()} returns {@code null}, the schedule was removed from the gate. When it
 * returns a non-{@code null} value, the schedule was set or updated to the returned value.
 *
 * <p><b>Reactive environments:</b> This event is published synchronously via {@link
 * org.springframework.context.ApplicationEventPublisher} from the actuator endpoint's management
 * thread. In WebFlux applications, listeners should avoid blocking the calling thread. If
 * long-running or reactive work is needed in response to schedule changes, offload it to a separate
 * scheduler (e.g., {@code Schedulers.boundedElastic()}) or publish a message to a reactive stream.
 */
public class EndpointGateScheduleChangedEvent extends ApplicationEvent {

  /** The identifier of the endpoint gate whose schedule was changed. */
  private final String gateId;

  /** The new schedule, or {@code null} if the schedule was removed. */
  @Nullable private final Schedule schedule;

  /**
   * Constructs an {@code EndpointGateScheduleChangedEvent}.
   *
   * @param source the object that published the event
   * @param gateId the identifier of the endpoint gate whose schedule was changed
   * @param schedule the new schedule, or {@code null} if the schedule was removed
   */
  public EndpointGateScheduleChangedEvent(
      Object source, String gateId, @Nullable Schedule schedule) {
    super(source);
    this.gateId = gateId;
    this.schedule = schedule;
  }

  /**
   * Returns the identifier of the endpoint gate whose schedule was changed.
   *
   * @return the gate identifier
   */
  public String gateId() {
    return gateId;
  }

  /**
   * Returns the new schedule, or {@code null} if the schedule was removed.
   *
   * @return the new {@link Schedule}, or {@code null}
   */
  @Nullable
  public Schedule schedule() {
    return schedule;
  }
}
