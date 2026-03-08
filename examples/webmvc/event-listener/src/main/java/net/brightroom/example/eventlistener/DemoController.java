package net.brightroom.example.eventlistener;

import java.util.List;
import java.util.Map;
import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

  private final AuditEventListener auditEventListener;
  private final EndpointGateCache cache;

  public DemoController(AuditEventListener auditEventListener, EndpointGateCache cache) {
    this.auditEventListener = auditEventListener;
    this.cache = cache;
  }

  @EndpointGate("demo-feature")
  @GetMapping("/demo")
  public String demo() {
    return "Demo feature is enabled!";
  }

  @GetMapping("/audit-log")
  public List<String> auditLog() {
    return auditEventListener.getAuditLog();
  }

  @GetMapping("/cache")
  public Map<String, Object> cacheState() {
    return cache.getAll();
  }
}
