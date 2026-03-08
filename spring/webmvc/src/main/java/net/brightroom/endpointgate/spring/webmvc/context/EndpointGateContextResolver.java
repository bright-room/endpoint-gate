package net.brightroom.endpointgate.spring.webmvc.context;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import net.brightroom.endpointgate.core.context.EndpointGateContext;

/**
 * Resolves the endpoint gate context from the current HTTP request.
 *
 * <p>The context is used for rollout percentage checks. The default implementation ({@code
 * RandomEndpointGateContextResolver}) generates a random UUID per request, providing non-sticky
 * (per-request probabilistic) rollout behavior.
 *
 * <p>To achieve sticky rollout (same user always gets the same result), implement this interface
 * and register it as a {@code @Bean}. The custom bean will replace the default due to
 * {@code @ConditionalOnMissingBean}.
 *
 * <p>Return {@link Optional#empty()} if the identifier cannot be resolved. In that case, the
 * rollout check is skipped and the gate is treated as fully enabled (fail-open).
 */
public interface EndpointGateContextResolver {

  /**
   * Resolves the endpoint gate context from the current request.
   *
   * @param request the current HTTP request
   * @return the resolved context, or {@link Optional#empty()} to skip the rollout check
   */
  Optional<EndpointGateContext> resolve(HttpServletRequest request);
}
