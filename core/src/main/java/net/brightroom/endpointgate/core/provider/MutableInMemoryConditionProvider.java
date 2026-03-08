package net.brightroom.endpointgate.core.provider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe, in-memory implementation of {@link MutableConditionProvider}.
 *
 * <p>Condition expressions are stored in a {@link ConcurrentHashMap}, allowing concurrent reads and
 * writes without external synchronization.
 */
public class MutableInMemoryConditionProvider implements MutableConditionProvider {

  private final ConcurrentHashMap<String, String> conditions;

  /** {@inheritDoc} */
  @Override
  public Optional<String> getCondition(String gateId) {
    return Optional.ofNullable(conditions.get(gateId));
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getConditions() {
    return Map.copyOf(conditions);
  }

  /** {@inheritDoc} */
  @Override
  public void setCondition(String gateId, String condition) {
    conditions.put(gateId, condition);
  }

  /** {@inheritDoc} */
  @Override
  public void removeCondition(String gateId) {
    conditions.remove(gateId);
  }

  /**
   * Constructs a {@code MutableInMemoryConditionProvider} with the given initial condition
   * expressions.
   *
   * @param conditions the initial condition expressions map; copied defensively on construction
   */
  public MutableInMemoryConditionProvider(Map<String, String> conditions) {
    this.conditions = new ConcurrentHashMap<>(conditions);
  }
}
