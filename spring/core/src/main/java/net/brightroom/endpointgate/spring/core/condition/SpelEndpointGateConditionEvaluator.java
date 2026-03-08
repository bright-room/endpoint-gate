package net.brightroom.endpointgate.spring.core.condition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.EndpointGateConditionEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

/**
 * Default {@link EndpointGateConditionEvaluator} implementation using Spring Expression Language
 * (SpEL).
 *
 * <p>Uses {@link SimpleEvaluationContext} with {@link DataBindingPropertyAccessor} (read-only) to
 * safely evaluate expressions against a typed {@link ConditionVariables} root object. Type
 * references ({@code T(...)}), constructors ({@code new ...}), and bean references ({@code
 * @beanName}) are structurally excluded.
 *
 * <p>Parsed expressions are cached in a {@link ConcurrentHashMap} for performance, since condition
 * expressions are static (annotation-derived).
 */
public class SpelEndpointGateConditionEvaluator implements EndpointGateConditionEvaluator {

  private static final Log log = LogFactory.getLog(SpelEndpointGateConditionEvaluator.class);
  private static final DataBindingPropertyAccessor READ_ONLY_ACCESSOR =
      DataBindingPropertyAccessor.forReadOnlyAccess();

  private final SpelExpressionParser parser = new SpelExpressionParser();
  private final ConcurrentMap<String, Expression> cache = new ConcurrentHashMap<>();
  private final boolean failOnError;

  /**
   * Creates a new {@code SpelEndpointGateConditionEvaluator}.
   *
   * @param failOnError when {@code true}, evaluation errors result in {@code false} (fail-closed);
   *     when {@code false}, errors result in {@code true} (fail-open)
   */
  public SpelEndpointGateConditionEvaluator(boolean failOnError) {
    this.failOnError = failOnError;
  }

  @Override
  public boolean evaluate(String expression, ConditionVariables variables) {
    try {
      Expression expr = cache.computeIfAbsent(expression, parser::parseExpression);
      SimpleEvaluationContext context =
          SimpleEvaluationContext.forPropertyAccessors(READ_ONLY_ACCESSOR)
              .withRootObject(variables)
              .build();
      Boolean result = expr.getValue(context, Boolean.class);
      return Boolean.TRUE.equals(result);
    } catch (EvaluationException | ParseException e) {
      log.warn(
          String.format(
              "Failed to evaluate endpoint gate condition expression '%s': %s",
              expression, e.getMessage()));
      return !failOnError;
    }
  }
}
