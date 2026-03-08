package net.brightroom.endpointgate.reactive.core.provider;

import reactor.core.publisher.Mono;

/**
 * Reactive SPI for resolving the condition expression for a given gate.
 *
 * <p>Implementations provide the configured condition expression for each gate. When a gate has no
 * configured condition, an empty {@link Mono} is returned and the gate is treated as having no
 * condition restriction.
 *
 * <p>Implement this interface and register it as a Spring bean to override the default in-memory
 * provider. For example, to read conditions from a reactive data source.
 */
public interface ReactiveConditionProvider {

  /**
   * Returns the configured condition expression for the specified gate.
   *
   * @param gateId the identifier of the gate
   * @return a {@link Mono} emitting the condition expression, or an empty {@link Mono} if no
   *     condition is configured for this gate
   */
  Mono<String> getCondition(String gateId);
}
