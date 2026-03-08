package net.brightroom.example.provider.simple;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("external-endpoint-gates")
public class ExternalConfigProperties {

  private Map<String, Boolean> gates = Map.of();

  public Map<String, Boolean> getGates() {
    return gates;
  }

  public void setGates(Map<String, Boolean> gates) {
    this.gates = gates;
  }
}
