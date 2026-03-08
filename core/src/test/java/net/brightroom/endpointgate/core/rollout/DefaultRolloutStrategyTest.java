package net.brightroom.endpointgate.core.rollout;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import org.junit.jupiter.api.Test;

class DefaultRolloutStrategyTest {

  private final DefaultRolloutStrategy strategy = new DefaultRolloutStrategy();
  private final EndpointGateContext context = new EndpointGateContext("user-1");

  @Test
  void isInRollout_returnsFalse_whenPercentageIsZero() {
    assertFalse(strategy.isInRollout("gate", context, 0));
  }

  @Test
  void isInRollout_returnsFalse_whenPercentageIsNegative() {
    assertFalse(strategy.isInRollout("gate", context, -1));
  }

  @Test
  void isInRollout_returnsTrue_whenPercentageIs100() {
    assertTrue(strategy.isInRollout("gate", context, 100));
  }

  @Test
  void isInRollout_returnsTrue_whenPercentageIsOver100() {
    assertTrue(strategy.isInRollout("gate", context, 101));
  }

  @Test
  void isInRollout_isDeterministic_sameInputAlwaysProducesSameResult() {
    boolean first = strategy.isInRollout("gate", context, 50);
    boolean second = strategy.isInRollout("gate", context, 50);
    assertEquals(first, second);
  }

  @Test
  void isInRollout_producesConsistentBucket_acrossMultipleGateIds() {
    EndpointGateContext ctx = new EndpointGateContext("stable-user");
    boolean resultA = strategy.isInRollout("gate-a", ctx, 50);
    boolean resultB = strategy.isInRollout("gate-b", ctx, 50);

    // Same user may have different buckets for different gates — just verify they're stable
    assertEquals(resultA, strategy.isInRollout("gate-a", ctx, 50));
    assertEquals(resultB, strategy.isInRollout("gate-b", ctx, 50));
  }

  @Test
  void isInRollout_bucketsAreInValidRange() {
    Set<Boolean> results = new HashSet<>();
    for (int i = 0; i < 200; i++) {
      EndpointGateContext ctx = new EndpointGateContext("user-" + i);
      results.add(strategy.isInRollout("gate", ctx, 50));
    }
    assertTrue(results.contains(true));
    assertTrue(results.contains(false));
  }

  @Test
  void isInRollout_doesNotThrow_forInputsThatMightProduceExtremeHashValues() {
    assertDoesNotThrow(
        () -> {
          for (int i = 0; i < 1000; i++) {
            EndpointGateContext ctx = new EndpointGateContext("probe-" + i);
            strategy.isInRollout("gate", ctx, 50);
          }
        });
  }

  @Test
  void isInRollout_returnsTrue_whenPercentageIs99_andBucketIsWithinRange() {
    int inRollout = 0;
    for (int i = 0; i < 100; i++) {
      EndpointGateContext ctx = new EndpointGateContext("user-" + i);
      if (strategy.isInRollout("gate", ctx, 99)) {
        inRollout++;
      }
    }
    assertTrue(inRollout > 80, "Expected more than 80% in rollout at 99%, got: " + inRollout);
  }

  @Test
  void isInRollout_returnsTrue_whenPercentageIs1_andBucketIsWithinRange() {
    int inRollout = 0;
    for (int i = 0; i < 100; i++) {
      EndpointGateContext ctx = new EndpointGateContext("user-" + i);
      if (strategy.isInRollout("gate", ctx, 1)) {
        inRollout++;
      }
    }
    assertTrue(inRollout < 20, "Expected fewer than 20% in rollout at 1%, got: " + inRollout);
  }
}
