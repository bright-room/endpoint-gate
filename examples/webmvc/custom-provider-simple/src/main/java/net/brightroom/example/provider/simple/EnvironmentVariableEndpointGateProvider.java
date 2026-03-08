package net.brightroom.example.provider.simple;

import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("env-variable")
public class EnvironmentVariableEndpointGateProvider implements EndpointGateProvider {

  @Override
  public boolean isGateEnabled(String gateId) {
    String envKey = "EG_" + gateId.toUpperCase().replace('-', '_');
    return "true".equalsIgnoreCase(System.getenv(envKey));
  }
}
