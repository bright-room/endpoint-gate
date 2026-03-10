package net.brightroom.example.actuator;

import net.brightroom.endpointgate.spring.core.event.EndpointGateChangedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateRemovedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateScheduleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EndpointGateEventListener {

  private static final Logger log = LoggerFactory.getLogger(EndpointGateEventListener.class);

  @EventListener
  public void onEndpointGateChanged(EndpointGateChangedEvent event) {
    if (event.rolloutPercentage() != null) {
      log.info(
          "[EndpointGateChangedEvent] gateId={}, enabled={}, rollout={}",
          event.gateId(),
          event.enabled(),
          event.rolloutPercentage());
    } else {
      log.info("[EndpointGateChangedEvent] gateId={}, enabled={}", event.gateId(), event.enabled());
    }
  }

  @EventListener
  public void onEndpointGateRemoved(EndpointGateRemovedEvent event) {
    log.info("[EndpointGateRemovedEvent] gateId={}", event.gateId());
  }

  @EventListener
  public void onEndpointGateScheduleChanged(EndpointGateScheduleChangedEvent event) {
    if (event.schedule() != null) {
      log.info(
          "[EndpointGateScheduleChangedEvent] gateId={}, start={}, end={}, timezone={}",
          event.gateId(),
          event.schedule().start(),
          event.schedule().end(),
          event.schedule().timezone());
    } else {
      log.info("[EndpointGateScheduleChangedEvent] gateId={}, schedule=removed", event.gateId());
    }
  }
}
