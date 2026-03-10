package net.brightroom.example.healthindicator;

import java.time.Instant;
import java.util.Map;
import net.brightroom.endpointgate.spring.actuator.health.ReactiveHealthDetailsContributor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CustomReactiveHealthDetailsContributor implements ReactiveHealthDetailsContributor {

  @Override
  public Mono<Map<String, Object>> contributeDetails() {
    return Mono.fromCallable(
        () -> Map.of("environment", "production", "lastChecked", Instant.now().toString()));
  }
}
