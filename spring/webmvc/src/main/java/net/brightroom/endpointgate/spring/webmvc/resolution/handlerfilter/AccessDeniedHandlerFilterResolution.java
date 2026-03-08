package net.brightroom.endpointgate.spring.webmvc.resolution.handlerfilter;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Interface for handling cases where access to an endpoint gate protected resource is denied in a
 * {@link HandlerFilterFunction} context.
 *
 * <p>Implementations return a {@link ServerResponse} that the functional web framework writes to
 * the client, rather than writing to the response directly.
 *
 * <p>To customize the denied response, implement this interface and register it as a {@code @Bean}.
 * The custom bean takes priority over the library's default implementation due to
 * {@code @ConditionalOnMissingBean}:
 *
 * <pre>{@code
 * @Bean
 * AccessDeniedHandlerFilterResolution myResolution() {
 *     return (request, e) -> ServerResponse.status(HttpStatus.FORBIDDEN)
 *         .body("Access denied: " + e.gateId());
 * }
 * }</pre>
 *
 * <p>Note: {@code ServerResponse.BodyBuilder.body(Object)} declares {@code throws
 * ServletException}. Wrap the call in a try-catch and rethrow as an unchecked exception if a
 * checked exception is required at the call site.
 */
public interface AccessDeniedHandlerFilterResolution {

  /**
   * Resolves the response when access to an endpoint gate protected resource is denied.
   *
   * @param request the current server request
   * @param e the EndpointGateAccessDeniedException that triggered the resolution
   * @return the {@link ServerResponse} to send to the client
   */
  ServerResponse resolve(ServerRequest request, EndpointGateAccessDeniedException e);
}
