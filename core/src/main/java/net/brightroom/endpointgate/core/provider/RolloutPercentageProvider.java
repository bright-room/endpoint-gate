package net.brightroom.endpointgate.core.provider;

import java.util.OptionalInt;

/**
 * SPI for resolving the rollout percentage for a given endpoint gate.
 *
 * <p>Implementations provide the configured rollout percentage for each gate. When a gate has no
 * configured rollout percentage, {@link OptionalInt#empty()} is returned.
 */
public interface RolloutPercentageProvider {

  /**
   * Returns the configured rollout percentage for the specified gate.
   *
   * @param gateId the identifier of the gate
   * @return an {@link OptionalInt} containing the rollout percentage (0–100), or {@link
   *     OptionalInt#empty()} if no rollout percentage is configured for this gate
   */
  OptionalInt getRolloutPercentage(String gateId);
}
