package net.brightroom.endpointgate.spring.core.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.condition.EndpointGateConditionEvaluator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpelReactiveEndpointGateConditionEvaluatorTest {

  @Mock EndpointGateConditionEvaluator delegate;

  private ConditionVariables buildVariables() {
    return new ConditionVariablesBuilder()
        .headers(new HashMap<>())
        .params(new HashMap<>())
        .cookies(new HashMap<>())
        .path("/test")
        .method("GET")
        .remoteAddress("127.0.0.1")
        .build();
  }

  @Test
  void evaluate_returnsTrue_whenDelegateReturnsTrue() {
    ConditionVariables variables = buildVariables();
    when(delegate.evaluate(eq("method == 'GET'"), any(ConditionVariables.class))).thenReturn(true);

    SpelReactiveEndpointGateConditionEvaluator evaluator =
        new SpelReactiveEndpointGateConditionEvaluator(delegate);

    Boolean result = evaluator.evaluate("method == 'GET'", variables).block();

    assertThat(result).isTrue();
    verify(delegate).evaluate(eq("method == 'GET'"), any(ConditionVariables.class));
  }

  @Test
  void evaluate_returnsFalse_whenDelegateReturnsFalse() {
    ConditionVariables variables = buildVariables();
    when(delegate.evaluate(eq("method == 'GET'"), any(ConditionVariables.class))).thenReturn(false);

    SpelReactiveEndpointGateConditionEvaluator evaluator =
        new SpelReactiveEndpointGateConditionEvaluator(delegate);

    Boolean result = evaluator.evaluate("method == 'GET'", variables).block();

    assertThat(result).isFalse();
    verify(delegate).evaluate(eq("method == 'GET'"), any(ConditionVariables.class));
  }
}
