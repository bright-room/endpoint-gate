package net.brightroom.example.eventlistener;

import net.brightroom.endpointgate.spring.core.event.EndpointGateChangedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CacheInvalidationListener {

  private static final Logger log = LoggerFactory.getLogger(CacheInvalidationListener.class);

  private final EndpointGateCache cache;

  public CacheInvalidationListener(EndpointGateCache cache) {
    this.cache = cache;
  }

  @EventListener
  public void onChanged(EndpointGateChangedEvent event) {
    log.info("Invalidating cache for gate: {}", event.gateId());
    cache.invalidate(event.gateId());
  }

  @EventListener
  public void onRemoved(EndpointGateRemovedEvent event) {
    log.info("Invalidating cache for removed gate: {}", event.gateId());
    cache.invalidate(event.gateId());
  }
}
