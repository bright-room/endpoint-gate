package net.brightroom.endpointgate.reactive.core.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;

/**
 * A thread-safe, in-memory implementation of {@link MutableReactiveConditionProvider}.
 *
 * <p>Condition expressions are stored in a {@link ConcurrentHashMap}, allowing concurrent reads and
 * writes without external synchronization.
 */
public class MutableInMemoryReactiveConditionProvider implements MutableReactiveConditionProvider {

  private final ConcurrentHashMap<String, String> conditions;

  /**
   * {@inheritDoc}
   *
   * <p>Returns an empty {@link Mono} for gates not present in the conditions map.
   */
  @Override
  public Mono<String> getCondition(String gateId) {
    String condition = conditions.get(gateId);
    return Mono.justOrEmpty(condition);
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Map<String, String>> getConditions() {
    return Mono.just(Map.copyOf(conditions));
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Void> setCondition(String gateId, String condition) {
    return Mono.<Void>fromRunnable(() -> conditions.put(gateId, condition));
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Boolean> removeCondition(String gateId) {
    return Mono.fromCallable(() -> conditions.remove(gateId) != null);
  }

  /**
   * Constructs a {@code MutableInMemoryReactiveConditionProvider} with the given initial condition
   * expressions.
   *
   * @param conditions the initial condition expressions map; copied defensively on construction
   */
  public MutableInMemoryReactiveConditionProvider(Map<String, String> conditions) {
    this.conditions = new ConcurrentHashMap<>(conditions);
  }
}
