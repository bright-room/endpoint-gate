package net.brightroom.example.customprovider;

import java.util.Map;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@EnableConfigurationProperties(ExternalConfigProperties.class)
public class ExternalConfigReactiveEndpointGateProvider implements ReactiveEndpointGateProvider {

  private final Map<String, Boolean> flags;

  public ExternalConfigReactiveEndpointGateProvider(ExternalConfigProperties properties) {
    this.flags = Map.copyOf(properties.getFlags());
  }

  @Override
  public Mono<Boolean> isGateEnabled(String gateId) {
    Boolean enabled = flags.get(gateId);
    if (enabled == null) {
      return Mono.empty();
    }
    return Mono.just(enabled);
  }
}
