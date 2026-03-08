package net.brightroom.example.rollout;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sticky")
public class UserIdContextResolver implements EndpointGateContextResolver {

  @Override
  public Optional<EndpointGateContext> resolve(HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter("userId"))
        .filter(id -> !id.isBlank())
        .map(EndpointGateContext::new);
  }
}
