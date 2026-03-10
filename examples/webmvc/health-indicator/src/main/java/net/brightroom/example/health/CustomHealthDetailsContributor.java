package net.brightroom.example.health;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import net.brightroom.endpointgate.spring.actuator.health.HealthDetailsContributor;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthDetailsContributor implements HealthDetailsContributor {

  @Override
  public Map<String, Object> contributeDetails() {
    Map<String, Object> details = new LinkedHashMap<>();
    details.put("environment", "production");
    details.put("lastChecked", Instant.now().toString());
    return details;
  }
}
