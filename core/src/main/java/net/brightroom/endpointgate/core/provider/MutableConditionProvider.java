package net.brightroom.endpointgate.core.provider;

import java.util.Map;

/**
 * An extension of {@link ConditionProvider} that supports dynamic mutation of condition expressions
 * at runtime.
 *
 * <p>Implementations must be thread-safe, as conditions may be read and updated concurrently.
 */
public interface MutableConditionProvider extends ConditionProvider {

  /**
   * Returns a snapshot of all currently configured condition expressions.
   *
   * <p>The returned map must be an immutable copy; mutations to the returned map must not affect
   * the provider's internal state.
   *
   * @return an immutable map of gate identifiers to their condition expressions
   */
  Map<String, String> getConditions();

  /**
   * Updates the condition expression for the specified gate.
   *
   * <p>If the gate does not have a configured condition, it is created.
   *
   * @param gateId the identifier of the gate to update
   * @param condition the new condition expression; must not be null
   */
  void setCondition(String gateId, String condition);

  /**
   * Removes the condition expression for the specified gate.
   *
   * <p>If the gate does not have a configured condition, this method is a no-op.
   *
   * @param gateId the identifier of the gate whose condition to remove
   */
  void removeCondition(String gateId);
}
