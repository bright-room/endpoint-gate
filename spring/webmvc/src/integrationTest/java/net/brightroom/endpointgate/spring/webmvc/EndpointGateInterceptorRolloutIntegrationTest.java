package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.rollout.DefaultRolloutStrategy;
import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateClassRolloutController;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateRolloutController;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Verifies rollout behavior through the full MVC stack:
 *
 * <ul>
 *   <li>Custom {@link EndpointGateContextResolver} bean is respected via
 *       {@code @ConditionalOnMissingBean}, enabling sticky rollout.
 *   <li>Rollout decision is deterministic for a fixed user identifier.
 *   <li>Class-level {@code @EndpointGate} with {@code rollout} is processed correctly by the
 *       interceptor.
 * </ul>
 */
@WebMvcTest(
    properties = {
      "endpoint-gate.gates.rollout-gate.enabled=true",
      "endpoint-gate.gates.rollout-gate.rollout=50"
    },
    controllers = {EndpointGateRolloutController.class, EndpointGateClassRolloutController.class})
@Import({
  EndpointGateMvcTestAutoConfiguration.class,
  EndpointGateInterceptorRolloutIntegrationTest.FixedContextResolverConfig.class
})
class EndpointGateInterceptorRolloutIntegrationTest {

  private static final EndpointGateContext FIXED_CONTEXT = new EndpointGateContext("fixed-user-id");
  private static final boolean IN_ROLLOUT_50 =
      new DefaultRolloutStrategy().isInRollout("rollout-gate", FIXED_CONTEXT, 50);

  @TestConfiguration
  static class FixedContextResolverConfig {
    @Bean
    EndpointGateContextResolver contextResolver() {
      return request -> Optional.of(FIXED_CONTEXT);
    }
  }

  MockMvc mockMvc;

  @Autowired
  EndpointGateInterceptorRolloutIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @Test
  void methodLevel_stickyRollout_returnsDeterministicResult_forFixedUserId() throws Exception {
    ResultMatcher expected = IN_ROLLOUT_50 ? status().isOk() : status().isForbidden();
    mockMvc.perform(get("/test/rollout")).andExpect(expected);
  }

  @Test
  void methodLevel_stickyRollout_sameUserAlwaysGetsSameResult() throws Exception {
    // Call twice — result must be identical (deterministic hashing)
    ResultMatcher expected = IN_ROLLOUT_50 ? status().isOk() : status().isForbidden();
    mockMvc.perform(get("/test/rollout")).andExpect(expected);
    mockMvc.perform(get("/test/rollout")).andExpect(expected);
  }

  @Test
  void classLevel_stickyRollout_returnsDeterministicResult_forFixedUserId() throws Exception {
    // Verifies that class-level @EndpointGate with rollout is processed through the interceptor
    ResultMatcher expected = IN_ROLLOUT_50 ? status().isOk() : status().isForbidden();
    mockMvc.perform(get("/test/class-rollout")).andExpect(expected);
  }

  @Test
  void methodLevel_rolloutAllowed_returnsBody_whenInRollout() throws Exception {
    // When the fixed user is in rollout, the response body should be "Allowed"
    Assumptions.assumeTrue(IN_ROLLOUT_50, "User not in rollout bucket");
    mockMvc
        .perform(get("/test/rollout"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }
}
