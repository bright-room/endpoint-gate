package net.brightroom.example.customrolloutstrategy;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("region")
public class RegionContextResolver implements EndpointGateContextResolver {

  @Override
  public Optional<EndpointGateContext> resolve(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader("X-Region"))
        .filter(region -> !region.isBlank())
        .map(EndpointGateContext::new);
  }
}
