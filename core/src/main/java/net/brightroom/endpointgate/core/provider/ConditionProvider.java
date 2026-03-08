package net.brightroom.endpointgate.core.provider;

import java.util.Optional;

/**
 * SPI for resolving the condition expression for a given endpoint gate.
 *
 * <p>Implementations provide the configured condition expression for each gate. When a gate has no
 * configured condition, {@link Optional#empty()} is returned and the gate is treated as having no
 * condition restriction.
 */
public interface ConditionProvider {

  /**
   * Returns the configured condition expression for the specified gate.
   *
   * @param gateId the identifier of the gate
   * @return an {@link Optional} containing the condition expression, or {@link Optional#empty()} if
   *     no condition is configured for this gate
   */
  Optional<String> getCondition(String gateId);
}
