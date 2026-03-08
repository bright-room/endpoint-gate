package net.brightroom.endpointgate.core.provider;

import java.util.Map;
import java.util.OptionalInt;

/**
 * An implementation of {@link RolloutPercentageProvider} that stores rollout percentages in memory
 * using a {@link Map}.
 */
public class InMemoryRolloutPercentageProvider implements RolloutPercentageProvider {

  private final Map<String, Integer> rolloutPercentages;

  /** {@inheritDoc} */
  @Override
  public OptionalInt getRolloutPercentage(String gateId) {
    Integer percentage = rolloutPercentages.get(gateId);
    if (percentage != null) {
      return OptionalInt.of(percentage);
    }
    return OptionalInt.empty();
  }

  /**
   * Constructs an instance with the provided rollout percentages.
   *
   * @param rolloutPercentages a map containing gate identifiers as keys and their rollout
   *     percentages as values; copied defensively on construction
   */
  public InMemoryRolloutPercentageProvider(Map<String, Integer> rolloutPercentages) {
    this.rolloutPercentages = Map.copyOf(rolloutPercentages);
  }
}
