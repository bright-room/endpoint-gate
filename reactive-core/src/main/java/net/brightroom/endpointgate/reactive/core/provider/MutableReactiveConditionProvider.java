package net.brightroom.endpointgate.reactive.core.provider;

import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * A reactive extension of {@link ReactiveConditionProvider} that supports dynamic mutation of
 * condition expressions at runtime.
 *
 * <p>Implementations must be thread-safe, as conditions may be read and updated concurrently.
 *
 * <p>This interface serves as an SPI for the actuator endpoint to update condition expressions at
 * runtime without restarting the application.
 */
public interface MutableReactiveConditionProvider extends ReactiveConditionProvider {

  /**
   * Returns a snapshot of all currently configured condition expressions.
   *
   * <p>The returned map must be an immutable copy; mutations to the returned map must not affect
   * the provider's internal state.
   *
   * @return a {@link Mono} emitting an immutable map of gate identifiers to their condition
   *     expressions
   */
  Mono<Map<String, String>> getConditions();

  /**
   * Updates the condition expression for the specified gate.
   *
   * <p>If the gate does not have a configured condition, it is created.
   *
   * @param gateId the identifier of the gate to update
   * @param condition the new condition expression; must not be null
   * @return a {@link Mono} that completes when the update is applied
   */
  Mono<Void> setCondition(String gateId, String condition);

  /**
   * Removes the condition expression for the specified gate.
   *
   * <p>If the gate does not have a configured condition, this method is a no-op and emits {@code
   * false}.
   *
   * @param gateId the identifier of the gate whose condition to remove
   * @return a {@link Mono} emitting {@code true} if the condition existed and was removed, {@code
   *     false} if it did not exist
   */
  Mono<Boolean> removeCondition(String gateId);
}
