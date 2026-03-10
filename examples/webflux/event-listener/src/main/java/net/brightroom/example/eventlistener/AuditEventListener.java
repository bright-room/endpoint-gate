package net.brightroom.example.eventlistener;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.brightroom.endpointgate.spring.core.event.EndpointGateChangedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateRemovedEvent;
import net.brightroom.endpointgate.spring.core.event.EndpointGateScheduleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

  private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

  private final List<String> auditLog = new CopyOnWriteArrayList<>();

  @EventListener
  public void onChanged(EndpointGateChangedEvent event) {
    String entry =
        String.format(
            "[%s] CHANGED: gate=%s, enabled=%s, rollout=%s",
            Instant.now(), event.gateId(), event.enabled(), event.rolloutPercentage());
    auditLog.add(entry);
    log.info(entry);
  }

  @EventListener
  public void onRemoved(EndpointGateRemovedEvent event) {
    String entry = String.format("[%s] REMOVED: gate=%s", Instant.now(), event.gateId());
    auditLog.add(entry);
    log.info(entry);
  }

  @EventListener
  public void onScheduleChanged(EndpointGateScheduleChangedEvent event) {
    String entry =
        String.format(
            "[%s] SCHEDULE_CHANGED: gate=%s, schedule=%s",
            Instant.now(), event.gateId(), event.schedule());
    auditLog.add(entry);
    log.info(entry);
  }

  public List<String> getAuditLog() {
    return List.copyOf(auditLog);
  }
}
