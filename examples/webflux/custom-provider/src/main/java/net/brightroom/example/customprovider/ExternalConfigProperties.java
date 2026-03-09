package net.brightroom.example.customprovider;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("external-endpoint-gates")
public class ExternalConfigProperties {

  private Map<String, Boolean> flags = Map.of();

  public Map<String, Boolean> getFlags() {
    return flags;
  }

  public void setFlags(Map<String, Boolean> flags) {
    this.flags = flags;
  }
}
