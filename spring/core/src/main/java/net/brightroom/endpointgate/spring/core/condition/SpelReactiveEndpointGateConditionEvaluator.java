package net.brightroom.endpointgate.spring.core.condition;

import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.EndpointGateConditionEvaluator;
import net.brightroom.endpointgate.reactive.core.condition.ReactiveEndpointGateConditionEvaluator;
import reactor.core.publisher.Mono;

/**
 * Default {@link ReactiveEndpointGateConditionEvaluator} that wraps a synchronous {@link
 * EndpointGateConditionEvaluator} via {@link Mono#fromCallable}.
 *
 * <p>This implementation is CPU-bound and runs on the subscribing thread. It is suitable for the
 * default SpEL-based evaluation which is fast. Custom implementations that need to perform
 * non-blocking I/O should implement {@link ReactiveEndpointGateConditionEvaluator} directly and
 * subscribe on an appropriate scheduler.
 */
public class SpelReactiveEndpointGateConditionEvaluator
    implements ReactiveEndpointGateConditionEvaluator {

  private final EndpointGateConditionEvaluator delegate;

  /**
   * Creates a new {@code SpelReactiveEndpointGateConditionEvaluator}.
   *
   * @param delegate the synchronous evaluator to delegate to; must not be null
   */
  public SpelReactiveEndpointGateConditionEvaluator(EndpointGateConditionEvaluator delegate) {
    this.delegate = delegate;
  }

  @Override
  public Mono<Boolean> evaluate(String expression, ConditionVariables variables) {
    return Mono.fromCallable(() -> delegate.evaluate(expression, variables));
  }
}
