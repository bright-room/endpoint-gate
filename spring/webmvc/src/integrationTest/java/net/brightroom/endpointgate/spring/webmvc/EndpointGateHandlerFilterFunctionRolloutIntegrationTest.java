package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.web.servlet.function.RouterFunctions.route;

import java.util.Optional;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.rollout.DefaultRolloutStrategy;
import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webmvc.filter.EndpointGateHandlerFilterFunction;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Verifies rollout behavior for Functional Endpoints through the full MVC stack:
 *
 * <ul>
 *   <li>Custom {@link EndpointGateContextResolver} bean is respected via
 *       {@code @ConditionalOnMissingBean}, enabling sticky rollout.
 *   <li>Rollout decision is deterministic for a fixed user identifier.
 *   <li>{@link EndpointGateHandlerFilterFunction#of(String, int)} correctly applies rollout control
 *       via {@code ServerRequest.servletRequest()} in the MVC pipeline.
 * </ul>
 */
@WebMvcTest(
    properties = {
      "endpoint-gate.gates.rollout-gate.enabled=true",
      "endpoint-gate.gates.rollout-gate.rollout=50"
    })
@Import({
  EndpointGateMvcTestAutoConfiguration.class,
  EndpointGateHandlerFilterFunctionRolloutIntegrationTest.FixedContextResolverConfig.class,
  EndpointGateHandlerFilterFunctionRolloutIntegrationTest.RolloutRouteConfig.class
})
class EndpointGateHandlerFilterFunctionRolloutIntegrationTest {

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

  @TestConfiguration
  static class RolloutRouteConfig {
    @Bean
    RouterFunction<ServerResponse> functionalRolloutTestRoute(
        EndpointGateHandlerFilterFunction endpointGateFilter) {
      return route()
          .GET("/functional/rollout-test", req -> ServerResponse.ok().body("Allowed"))
          .filter(endpointGateFilter.of("rollout-gate", 50))
          .build();
    }
  }

  MockMvc mockMvc;

  @Autowired
  EndpointGateHandlerFilterFunctionRolloutIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @Test
  void rollout_returnsDeterministicResult_forFixedUserId() throws Exception {
    ResultMatcher expected =
        IN_ROLLOUT_50
            ? MockMvcResultMatchers.status().isOk()
            : MockMvcResultMatchers.status().isForbidden();
    mockMvc.perform(MockMvcRequestBuilders.get("/functional/rollout-test")).andExpect(expected);
  }

  @Test
  void rollout_sameUserAlwaysGetsSameResult() throws Exception {
    // Call twice — result must be identical (deterministic hashing)
    ResultMatcher expected =
        IN_ROLLOUT_50
            ? MockMvcResultMatchers.status().isOk()
            : MockMvcResultMatchers.status().isForbidden();
    mockMvc.perform(MockMvcRequestBuilders.get("/functional/rollout-test")).andExpect(expected);
    mockMvc.perform(MockMvcRequestBuilders.get("/functional/rollout-test")).andExpect(expected);
  }

  @Test
  void rollout_returnsBody_whenInRollout() throws Exception {
    Assumptions.assumeTrue(IN_ROLLOUT_50, "User not in rollout bucket");
    mockMvc
        .perform(MockMvcRequestBuilders.get("/functional/rollout-test"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("Allowed"));
  }
}
