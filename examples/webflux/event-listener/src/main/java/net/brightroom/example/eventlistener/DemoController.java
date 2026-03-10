package net.brightroom.example.eventlistener;

import java.util.List;
import java.util.Map;
import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
  public Mono<String> demo() {
    return Mono.just("Demo feature is enabled!");
  }

  @GetMapping("/audit-log")
  public Mono<List<String>> auditLog() {
    return Mono.just(auditEventListener.getAuditLog());
  }

  @GetMapping("/cache")
  public Mono<Map<String, Object>> cacheState() {
    return Mono.just(cache.getAll());
  }
}
