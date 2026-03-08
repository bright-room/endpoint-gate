package net.brightroom.endpointgate.reactive.core.condition;

import net.brightroom.endpointgate.core.condition.ConditionVariables;
import reactor.core.publisher.Mono;

/**
 * Reactive SPI for evaluating condition expressions against request context variables.
 *
 * <p>Implementations evaluate an expression with the given variables and return a {@link
 * Mono}{@code <Boolean>} indicating whether the condition is satisfied.
 *
 * <p>Register a custom bean to replace the default implementation:
 *
 * <pre>{@code
 * @Bean
 * ReactiveEndpointGateConditionEvaluator customEvaluator() {
 *     return (expression, variables) -> ...;
 * }
 * }</pre>
 *
 * <p>Custom implementations that perform non-blocking I/O (e.g., querying a remote condition
 * service) should return a {@link Mono} that executes on an appropriate scheduler and must not
 * block the event loop thread.
 */
public interface ReactiveEndpointGateConditionEvaluator {

  /**
   * Evaluates a condition expression against the given variables.
   *
   * @param expression the expression to evaluate
   * @param variables the variables available in the expression context
   * @return a {@link Mono} emitting {@code true} if the condition is satisfied
   */
  Mono<Boolean> evaluate(String expression, ConditionVariables variables);
}
