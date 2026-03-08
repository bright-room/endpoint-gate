package net.brightroom.example.provider.database;

import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import org.springframework.stereotype.Component;

@Component
public class DatabaseEndpointGateProvider implements EndpointGateProvider {

  private final GateManagementMapper mapper;

  public DatabaseEndpointGateProvider(GateManagementMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public boolean isGateEnabled(String gateId) {
    Boolean enabled = mapper.findEnabledByGateId(gateId);
    return Boolean.TRUE.equals(enabled);
  }
}
