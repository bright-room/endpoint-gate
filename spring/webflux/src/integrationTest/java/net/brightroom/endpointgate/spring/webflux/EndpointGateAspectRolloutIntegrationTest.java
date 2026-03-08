package net.brightroom.endpointgate.spring.webflux;

import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.rollout.DefaultRolloutStrategy;
import net.brightroom.endpointgate.spring.webflux.context.ReactiveEndpointGateContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * Verifies rollout behavior through the full WebFlux stack with a real Netty server:
 *
 * <ul>
 *   <li>Custom {@link ReactiveEndpointGateContextResolver} bean is respected via
 *       {@code @ConditionalOnMissingBean}, enabling sticky rollout.
 *   <li>Rollout decision is deterministic for a fixed user identifier.
 *   <li>Class-level {@code @EndpointGate} with {@code rollout} is processed correctly by the
 *       aspect.
 * </ul>
 *
 * <p>A real Netty server is used (instead of {@code @WebFluxTest}) to ensure the full Spring
 * WebFlux pipeline — including {@code DispatcherHandler}'s Reactor context propagation of {@code
 * ServerWebExchange} — is exercised.
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "endpoint-gate.gates.rollout-feature.enabled=true",
      "endpoint-gate.gates.rollout-feature.rollout=50"
    })
class EndpointGateAspectRolloutIntegrationTest {

  private static final EndpointGateContext FIXED_CONTEXT = new EndpointGateContext("fixed-user-id");
  private static final boolean IN_ROLLOUT_50 =
      new DefaultRolloutStrategy().isInRollout("rollout-feature", FIXED_CONTEXT, 50);

  @TestConfiguration
  static class FixedContextResolverConfig {
    @Bean
    ReactiveEndpointGateContextResolver contextResolver() {
      return request -> Mono.just(FIXED_CONTEXT);
    }
  }

  @LocalServerPort int port;

  WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void methodLevel_stickyRollout_returnsDeterministicResult_forFixedUserId() {
    if (IN_ROLLOUT_50) {
      webTestClient.get().uri("/test/rollout").exchange().expectStatus().isOk();
    } else {
      webTestClient.get().uri("/test/rollout").exchange().expectStatus().isForbidden();
    }
  }

  @Test
  void methodLevel_stickyRollout_sameUserAlwaysGetsSameResult() {
    // Call twice — result must be identical (deterministic hashing)
    for (int i = 0; i < 2; i++) {
      if (IN_ROLLOUT_50) {
        webTestClient.get().uri("/test/rollout").exchange().expectStatus().isOk();
      } else {
        webTestClient.get().uri("/test/rollout").exchange().expectStatus().isForbidden();
      }
    }
  }

  @Test
  void classLevel_stickyRollout_returnsDeterministicResult_forFixedUserId() {
    // Verifies that class-level @EndpointGate with rollout is processed through the aspect
    if (IN_ROLLOUT_50) {
      webTestClient.get().uri("/test/class-rollout").exchange().expectStatus().isOk();
    } else {
      webTestClient.get().uri("/test/class-rollout").exchange().expectStatus().isForbidden();
    }
  }
}
