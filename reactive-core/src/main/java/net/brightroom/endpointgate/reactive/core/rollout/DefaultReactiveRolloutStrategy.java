package net.brightroom.endpointgate.reactive.core.rollout;

import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.rollout.DefaultRolloutStrategy;
import reactor.core.publisher.Mono;

/**
 * Default {@link ReactiveRolloutStrategy} that delegates to {@link DefaultRolloutStrategy}.
 *
 * <p>Wraps the synchronous SHA-256 hash bucketing logic in a non-blocking {@link Mono}.
 */
public class DefaultReactiveRolloutStrategy implements ReactiveRolloutStrategy {

  /** Creates a new {@code DefaultReactiveRolloutStrategy}. */
  public DefaultReactiveRolloutStrategy() {}

  private final DefaultRolloutStrategy delegate = new DefaultRolloutStrategy();

  @Override
  public Mono<Boolean> isInRollout(String gateId, EndpointGateContext context, int percentage) {
    return Mono.fromCallable(() -> delegate.isInRollout(gateId, context, percentage));
  }
}
