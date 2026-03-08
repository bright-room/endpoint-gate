package net.brightroom.example.provider.simple;

import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("external-config")
@EnableConfigurationProperties(ExternalConfigProperties.class)
public class ExternalConfigEndpointGateProvider implements EndpointGateProvider {

  private final ExternalConfigProperties properties;

  public ExternalConfigEndpointGateProvider(ExternalConfigProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean isGateEnabled(String gateId) {
    return Boolean.TRUE.equals(properties.getGates().get(gateId));
  }
}
