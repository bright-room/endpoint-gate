package net.brightroom.endpointgate.core.condition;

/**
 * SPI for evaluating condition expressions against request context variables.
 *
 * <p>Implementations evaluate an expression with the given variables and return whether the
 * condition is satisfied.
 *
 * <p>Register a custom bean to replace the default implementation:
 *
 * <pre>{@code
 * @Bean
 * EndpointGateConditionEvaluator customEvaluator() {
 *     return (expression, variables) -> ...;
 * }
 * }</pre>
 */
public interface EndpointGateConditionEvaluator {

  /**
   * Evaluates a condition expression against the given variables.
   *
   * @param expression the expression to evaluate
   * @param variables the variables available in the expression context
   * @return {@code true} if the condition is satisfied
   */
  boolean evaluate(String expression, ConditionVariables variables);
}
