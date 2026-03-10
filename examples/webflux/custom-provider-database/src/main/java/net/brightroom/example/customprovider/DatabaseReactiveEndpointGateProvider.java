package net.brightroom.example.customprovider;

import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DatabaseReactiveEndpointGateProvider implements ReactiveEndpointGateProvider {

  private final GateManagementRepository repository;

  public DatabaseReactiveEndpointGateProvider(GateManagementRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<Boolean> isGateEnabled(String gateId) {
    return repository.findById(gateId).map(GateManagementEntity::isEnabled);
  }
}
