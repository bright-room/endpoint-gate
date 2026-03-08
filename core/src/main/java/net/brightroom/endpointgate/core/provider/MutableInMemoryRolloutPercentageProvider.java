package net.brightroom.endpointgate.core.provider;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe, in-memory implementation of {@link MutableRolloutPercentageProvider}.
 *
 * <p>Rollout percentages are stored in a {@link ConcurrentHashMap}, allowing concurrent reads and
 * writes without external synchronization.
 */
public class MutableInMemoryRolloutPercentageProvider implements MutableRolloutPercentageProvider {

  private final ConcurrentHashMap<String, Integer> rolloutPercentages;

  /** {@inheritDoc} */
  @Override
  public OptionalInt getRolloutPercentage(String gateId) {
    Integer percentage = rolloutPercentages.get(gateId);
    if (percentage != null) {
      return OptionalInt.of(percentage);
    }
    return OptionalInt.empty();
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, Integer> getRolloutPercentages() {
    return Map.copyOf(rolloutPercentages);
  }

  /** {@inheritDoc} */
  @Override
  public void setRolloutPercentage(String gateId, int percentage) {
    if (percentage < 0 || percentage > 100) {
      throw new IllegalArgumentException(
          "percentage must be between 0 and 100, but was: " + percentage);
    }
    rolloutPercentages.put(gateId, percentage);
  }

  /** {@inheritDoc} */
  @Override
  public void removeRolloutPercentage(String gateId) {
    rolloutPercentages.remove(gateId);
  }

  /**
   * Constructs a {@code MutableInMemoryRolloutPercentageProvider} with the given initial rollout
   * percentages.
   *
   * @param rolloutPercentages the initial rollout percentage map; copied defensively on
   *     construction
   */
  public MutableInMemoryRolloutPercentageProvider(Map<String, Integer> rolloutPercentages) {
    this.rolloutPercentages = new ConcurrentHashMap<>(rolloutPercentages);
  }
}
