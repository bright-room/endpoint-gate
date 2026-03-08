package net.brightroom.endpointgate.spring.core.event;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an endpoint gate's enabled state, rollout percentage, or condition is
 * changed at runtime.
 *
 * <p>Listeners can subscribe to this event via {@code @EventListener} to react to gate state
 * changes (e.g., clearing caches, logging audit trails, or updating dependent systems).
 *
 * <p><b>Reactive environments:</b> This event is published synchronously via {@link
 * org.springframework.context.ApplicationEventPublisher} from the actuator endpoint's management
 * thread. In WebFlux applications, listeners should avoid blocking the calling thread. If
 * long-running or reactive work is needed in response to gate changes, offload it to a separate
 * scheduler (e.g., {@code Schedulers.boundedElastic()}) or publish a message to a reactive stream.
 */
public class EndpointGateChangedEvent extends ApplicationEvent {

  /** The identifier of the endpoint gate that was changed. */
  private final String gateId;

  /** Whether the endpoint gate is enabled after this change. */
  private final boolean enabled;

  /** The new rollout percentage, or {@code null} if the rollout was not changed. */
  @Nullable private final Integer rolloutPercentage;

  /** The new condition expression, or {@code null} if the condition was not changed. */
  @Nullable private final String condition;

  /**
   * Constructs an {@code EndpointGateChangedEvent} when only the enabled state changed.
   *
   * @param source the object that published the event
   * @param gateId the identifier of the endpoint gate that was changed
   * @param enabled the new enabled state of the endpoint gate
   */
  public EndpointGateChangedEvent(Object source, String gateId, boolean enabled) {
    this(source, gateId, enabled, null, null);
  }

  /**
   * Constructs an {@code EndpointGateChangedEvent} with an optional rollout percentage change.
   *
   * @param source the object that published the event
   * @param gateId the identifier of the endpoint gate that was changed
   * @param enabled the new enabled state of the endpoint gate
   * @param rolloutPercentage the new rollout percentage, or {@code null} if the rollout was not
   *     changed
   */
  public EndpointGateChangedEvent(
      Object source, String gateId, boolean enabled, @Nullable Integer rolloutPercentage) {
    this(source, gateId, enabled, rolloutPercentage, null);
  }

  /**
   * Constructs an {@code EndpointGateChangedEvent} with optional rollout percentage and condition
   * changes.
   *
   * @param source the object that published the event
   * @param gateId the identifier of the endpoint gate that was changed
   * @param enabled the new enabled state of the endpoint gate
   * @param rolloutPercentage the new rollout percentage, or {@code null} if the rollout was not
   *     changed
   * @param condition the new condition expression, or {@code null} if the condition was not changed
   */
  public EndpointGateChangedEvent(
      Object source,
      String gateId,
      boolean enabled,
      @Nullable Integer rolloutPercentage,
      @Nullable String condition) {
    super(source);
    this.gateId = gateId;
    this.enabled = enabled;
    this.rolloutPercentage = rolloutPercentage;
    this.condition = condition;
  }

  /**
   * Returns the identifier of the endpoint gate that was changed.
   *
   * @return the gate identifier
   */
  public String gateId() {
    return gateId;
  }

  /**
   * Returns the new enabled state of the endpoint gate.
   *
   * @return {@code true} if the gate was enabled, {@code false} if disabled
   */
  public boolean enabled() {
    return enabled;
  }

  /**
   * Returns the new rollout percentage, or {@code null} if the rollout was not changed.
   *
   * @return the rollout percentage (0–100), or {@code null}
   */
  @Nullable
  public Integer rolloutPercentage() {
    return rolloutPercentage;
  }

  /**
   * Returns the new condition expression, or {@code null} if the condition was not changed.
   *
   * @return the SpEL condition expression, or {@code null}
   */
  @Nullable
  public String condition() {
    return condition;
  }
}
