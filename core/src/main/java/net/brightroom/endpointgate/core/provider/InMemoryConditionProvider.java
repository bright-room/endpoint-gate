package net.brightroom.endpointgate.core.provider;

import java.util.Map;
import java.util.Optional;

/**
 * An implementation of {@link ConditionProvider} that stores condition expressions in memory using
 * a {@link Map}.
 */
public class InMemoryConditionProvider implements ConditionProvider {

  private final Map<String, String> conditions;

  /** {@inheritDoc} */
  @Override
  public Optional<String> getCondition(String gateId) {
    return Optional.ofNullable(conditions.get(gateId));
  }

  /**
   * Constructs an instance with the provided condition expressions.
   *
   * @param conditions a map containing gate identifiers as keys and their condition expressions as
   *     values; copied defensively on construction
   */
  public InMemoryConditionProvider(Map<String, String> conditions) {
    this.conditions = Map.copyOf(conditions);
  }
}
