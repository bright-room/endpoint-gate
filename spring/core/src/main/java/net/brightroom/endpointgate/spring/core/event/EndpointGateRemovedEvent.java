package net.brightroom.endpointgate.spring.core.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when an endpoint gate is removed at runtime.
 *
 * <p>Listeners can subscribe to this event via {@code @EventListener} to react to gate removal
 * (e.g., purging caches, logging audit trails, or updating dependent systems).
 *
 * <p><b>Reactive environments:</b> This event is published synchronously via {@link
 * org.springframework.context.ApplicationEventPublisher} from the actuator endpoint's management
 * thread. In WebFlux applications, listeners should avoid blocking the calling thread. If
 * long-running or reactive work is needed in response to gate removal, offload it to a separate
 * scheduler (e.g., {@code Schedulers.boundedElastic()}) or publish a message to a reactive stream.
 */
public class EndpointGateRemovedEvent extends ApplicationEvent {

  /** The identifier of the endpoint gate that was removed. */
  private final String gateId;

  /**
   * Constructs an {@code EndpointGateRemovedEvent}.
   *
   * @param source the object that published the event
   * @param gateId the identifier of the endpoint gate that was removed
   */
  public EndpointGateRemovedEvent(Object source, String gateId) {
    super(source);
    this.gateId = gateId;
  }

  /**
   * Returns the identifier of the endpoint gate that was removed.
   *
   * @return the gate identifier
   */
  public String gateId() {
    return gateId;
  }
}
