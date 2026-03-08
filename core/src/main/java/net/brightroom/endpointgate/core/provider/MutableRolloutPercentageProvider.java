package net.brightroom.endpointgate.core.provider;

import java.util.Map;

/**
 * An extension of {@link RolloutPercentageProvider} that supports dynamic mutation of rollout
 * percentages at runtime.
 *
 * <p>Implementations must be thread-safe, as rollout percentages may be read and updated
 * concurrently.
 */
public interface MutableRolloutPercentageProvider extends RolloutPercentageProvider {

  /**
   * Returns a snapshot of all currently configured rollout percentages.
   *
   * <p>The returned map must be an immutable copy; mutations to the returned map must not affect
   * the provider's internal state.
   *
   * @return an immutable map of gate identifiers to their rollout percentages
   */
  Map<String, Integer> getRolloutPercentages();

  /**
   * Updates the rollout percentage for the specified gate.
   *
   * <p>If the gate does not have a configured rollout percentage, it is created.
   *
   * @param gateId the identifier of the gate to update
   * @param percentage the new rollout percentage (0–100)
   */
  void setRolloutPercentage(String gateId, int percentage);

  /**
   * Removes the rollout percentage for the specified gate.
   *
   * <p>If the gate does not have a configured rollout percentage, this method is a no-op.
   *
   * @param gateId the identifier of the gate whose rollout percentage to remove
   */
  void removeRolloutPercentage(String gateId);
}
