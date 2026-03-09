package net.brightroom.endpointgate.spring.actuator.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import net.brightroom.endpointgate.core.provider.InMemoryEndpointGateProvider;
import net.brightroom.endpointgate.core.provider.MutableEndpointGateProvider;
import net.brightroom.endpointgate.core.provider.MutableInMemoryEndpointGateProvider;
import net.brightroom.endpointgate.core.provider.MutableInMemoryRolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.MutableInMemoryScheduleProvider;
import net.brightroom.endpointgate.core.provider.MutableRolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.RolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.ScheduleProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveScheduleProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveScheduleProvider;
import net.brightroom.endpointgate.spring.actuator.endpoint.EndpointGateEndpoint;
import net.brightroom.endpointgate.spring.actuator.endpoint.ReactiveEndpointGateEndpoint;
import net.brightroom.endpointgate.spring.actuator.health.EndpointGateHealthIndicator;
import net.brightroom.endpointgate.spring.actuator.health.HealthDetailsContributor;
import net.brightroom.endpointgate.spring.actuator.health.ReactiveEndpointGateHealthIndicator;
import net.brightroom.endpointgate.spring.actuator.health.ReactiveHealthDetailsContributor;
import net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import reactor.core.publisher.Mono;

class EndpointGateActuatorAutoConfigurationTest {

  @Nested
  class ServletTests {

    private final WebApplicationContextRunner contextRunner =
        new WebApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    EndpointGateAutoConfiguration.class,
                    EndpointGateActuatorAutoConfiguration.class));

    @Test
    void registersServletProviderAndEndpoint() {
      contextRunner.run(
          context -> {
            assertThat(context).hasSingleBean(MutableInMemoryEndpointGateProvider.class);
            assertThat(context).hasSingleBean(MutableInMemoryRolloutPercentageProvider.class);
            assertThat(context).hasSingleBean(MutableInMemoryScheduleProvider.class);
            assertThat(context).hasSingleBean(EndpointGateEndpoint.class);
            assertThat(context).doesNotHaveBean(ReactiveEndpointGateEndpoint.class);
          });
    }

    @Test
    void scheduleProvider_isMutableInMemoryScheduleProvider() {
      contextRunner.run(
          context -> {
            assertThat(context).hasSingleBean(ScheduleProvider.class);
            assertThat(context.getBean(ScheduleProvider.class))
                .isInstanceOf(MutableInMemoryScheduleProvider.class);
          });
    }

    @Test
    void endpointNotCreated_whenNonMutableProviderExists() {
      contextRunner
          .withBean(
              EndpointGateProvider.class, () -> new InMemoryEndpointGateProvider(Map.of(), false))
          .run(
              context -> {
                assertThat(context).doesNotHaveBean(EndpointGateEndpoint.class);
                assertThat(context).doesNotHaveBean(MutableInMemoryEndpointGateProvider.class);
                assertThat(context).hasSingleBean(MutableInMemoryRolloutPercentageProvider.class);
              });
    }

    @Test
    void customMutableProvider_usedByEndpoint_andDefaultNotRegistered() {
      var customProvider = new StubMutableEndpointGateProvider();
      contextRunner
          .withBean(MutableEndpointGateProvider.class, () -> customProvider)
          .run(
              context -> {
                assertThat(context).hasSingleBean(EndpointGateEndpoint.class);
                assertThat(context).doesNotHaveBean(MutableInMemoryEndpointGateProvider.class);
                assertThat(context.getBean(MutableEndpointGateProvider.class))
                    .isSameAs(customProvider);
              });
    }

    @Test
    void registersHealthIndicator() {
      contextRunner.run(
          context -> {
            assertThat(context).hasSingleBean(EndpointGateHealthIndicator.class);
          });
    }

    @Test
    void healthIndicatorNotCreated_whenDisabledViaProperty() {
      contextRunner
          .withPropertyValues("management.health.endpointGate.enabled=false")
          .run(
              context -> {
                assertThat(context).doesNotHaveBean(EndpointGateHealthIndicator.class);
              });
    }

    @Test
    void healthIndicatorRegistered_whenNonMutableProviderExists() {
      contextRunner
          .withBean(
              EndpointGateProvider.class, () -> new InMemoryEndpointGateProvider(Map.of(), false))
          .run(
              context -> {
                assertThat(context).hasSingleBean(EndpointGateHealthIndicator.class);
              });
    }

    @Test
    void healthIndicator_includesContributorDetails_whenContributorBeanPresent() {
      contextRunner
          .withBean(HealthDetailsContributor.class, () -> () -> Map.of("custom", "value"))
          .run(
              context -> {
                var indicator = context.getBean(EndpointGateHealthIndicator.class);
                Health health = indicator.health();
                assertThat(health.getDetails()).containsEntry("custom", "value");
              });
    }

    @Test
    void customRolloutProvider_defaultRolloutProviderNotRegistered() {
      var customRolloutProvider = new StubMutableRolloutPercentageProvider();
      contextRunner
          .withBean(MutableRolloutPercentageProvider.class, () -> customRolloutProvider)
          .run(
              context -> {
                assertThat(context).doesNotHaveBean(MutableInMemoryRolloutPercentageProvider.class);
                assertThat(context.getBean(RolloutPercentageProvider.class))
                    .isSameAs(customRolloutProvider);
              });
    }
  }

  @Nested
  class ReactiveTests {

    private final ReactiveWebApplicationContextRunner contextRunner =
        new ReactiveWebApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    EndpointGateAutoConfiguration.class,
                    EndpointGateActuatorAutoConfiguration.class));

    @Test
    void registersReactiveProviderAndEndpoint() {
      contextRunner.run(
          context -> {
            assertThat(context).hasSingleBean(MutableInMemoryReactiveEndpointGateProvider.class);
            assertThat(context)
                .hasSingleBean(MutableInMemoryReactiveRolloutPercentageProvider.class);
            assertThat(context).hasSingleBean(MutableInMemoryReactiveScheduleProvider.class);
            assertThat(context).hasSingleBean(ReactiveEndpointGateEndpoint.class);
            assertThat(context).doesNotHaveBean(EndpointGateEndpoint.class);
          });
    }

    @Test
    void reactiveScheduleProvider_isMutableInMemoryReactiveScheduleProvider() {
      contextRunner.run(
          context -> {
            assertThat(context).hasSingleBean(ReactiveScheduleProvider.class);
            assertThat(context.getBean(ReactiveScheduleProvider.class))
                .isInstanceOf(MutableInMemoryReactiveScheduleProvider.class);
          });
    }

    @Test
    void endpointNotCreated_whenCustomReactiveProviderExists() {
      contextRunner
          .withBean(ReactiveEndpointGateProvider.class, () -> gateId -> Mono.just(false))
          .run(
              context -> {
                assertThat(context).doesNotHaveBean(ReactiveEndpointGateEndpoint.class);
                assertThat(context)
                    .doesNotHaveBean(MutableInMemoryReactiveEndpointGateProvider.class);
              });
    }

    @Test
    void customMutableReactiveProvider_usedByEndpoint_andDefaultNotRegistered() {
      var customProvider = new StubMutableReactiveEndpointGateProvider();
      contextRunner
          .withBean(MutableReactiveEndpointGateProvider.class, () -> customProvider)
          .run(
              context -> {
                assertThat(context).hasSingleBean(ReactiveEndpointGateEndpoint.class);
                assertThat(context)
                    .doesNotHaveBean(MutableInMemoryReactiveEndpointGateProvider.class);
                assertThat(context.getBean(MutableReactiveEndpointGateProvider.class))
                    .isSameAs(customProvider);
              });
    }

    @Test
    void registersReactiveHealthIndicator() {
      contextRunner.run(
          context -> {
            assertThat(context).hasSingleBean(ReactiveEndpointGateHealthIndicator.class);
          });
    }

    @Test
    void reactiveHealthIndicatorNotCreated_whenDisabledViaProperty() {
      contextRunner
          .withPropertyValues("management.health.endpointGate.enabled=false")
          .run(
              context -> {
                assertThat(context).doesNotHaveBean(ReactiveEndpointGateHealthIndicator.class);
              });
    }

    @Test
    void reactiveHealthIndicatorRegistered_whenNonMutableProviderExists() {
      contextRunner
          .withBean(ReactiveEndpointGateProvider.class, () -> gateId -> Mono.just(false))
          .run(
              context -> {
                assertThat(context).hasSingleBean(ReactiveEndpointGateHealthIndicator.class);
              });
    }

    @Test
    void reactiveHealthIndicator_includesContributorDetails_whenContributorBeanPresent() {
      contextRunner
          .withBean(
              ReactiveHealthDetailsContributor.class,
              () -> () -> Mono.just(Map.of("custom", "value")))
          .run(
              context -> {
                var indicator = context.getBean(ReactiveEndpointGateHealthIndicator.class);
                Health health = indicator.health().block();
                assertThat(health.getDetails()).containsEntry("custom", "value");
              });
    }

    @Test
    void customReactiveRolloutProvider_defaultReactiveRolloutProviderNotRegistered() {
      var customRolloutProvider = new StubMutableReactiveRolloutPercentageProvider();
      contextRunner
          .withBean(MutableReactiveRolloutPercentageProvider.class, () -> customRolloutProvider)
          .run(
              context -> {
                assertThat(context)
                    .doesNotHaveBean(MutableInMemoryReactiveRolloutPercentageProvider.class);
                assertThat(context.getBean(ReactiveRolloutPercentageProvider.class))
                    .isSameAs(customRolloutProvider);
              });
    }
  }

  static class StubMutableEndpointGateProvider implements MutableEndpointGateProvider {

    private final Map<String, Boolean> store = new ConcurrentHashMap<>();

    @Override
    public boolean isGateEnabled(String gateId) {
      return store.getOrDefault(gateId, false);
    }

    @Override
    public Map<String, Boolean> getGates() {
      return Map.copyOf(store);
    }

    @Override
    public void setGateEnabled(String gateId, boolean enabled) {
      store.put(gateId, enabled);
    }

    @Override
    public boolean removeGate(String gateId) {
      return store.remove(gateId) != null;
    }
  }

  static class StubMutableReactiveEndpointGateProvider
      implements MutableReactiveEndpointGateProvider {

    private final Map<String, Boolean> store = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> isGateEnabled(String gateId) {
      return Mono.just(store.getOrDefault(gateId, false));
    }

    @Override
    public Mono<Map<String, Boolean>> getGates() {
      return Mono.just(Map.copyOf(store));
    }

    @Override
    public Mono<Void> setGateEnabled(String gateId, boolean enabled) {
      store.put(gateId, enabled);
      return Mono.empty();
    }

    @Override
    public Mono<Boolean> removeGate(String gateId) {
      return Mono.just(store.remove(gateId) != null);
    }
  }

  static class StubMutableRolloutPercentageProvider implements MutableRolloutPercentageProvider {

    private final Map<String, Integer> store = new ConcurrentHashMap<>();

    @Override
    public OptionalInt getRolloutPercentage(String gateId) {
      Integer pct = store.get(gateId);
      return pct != null ? OptionalInt.of(pct) : OptionalInt.empty();
    }

    @Override
    public Map<String, Integer> getRolloutPercentages() {
      return Map.copyOf(store);
    }

    @Override
    public void setRolloutPercentage(String gateId, int percentage) {
      store.put(gateId, percentage);
    }

    @Override
    public void removeRolloutPercentage(String gateId) {
      store.remove(gateId);
    }
  }

  static class StubMutableReactiveRolloutPercentageProvider
      implements MutableReactiveRolloutPercentageProvider {

    private final Map<String, Integer> store = new ConcurrentHashMap<>();

    @Override
    public Mono<Integer> getRolloutPercentage(String gateId) {
      Integer pct = store.get(gateId);
      return pct != null ? Mono.just(pct) : Mono.empty();
    }

    @Override
    public Mono<Map<String, Integer>> getRolloutPercentages() {
      return Mono.just(Map.copyOf(store));
    }

    @Override
    public Mono<Void> setRolloutPercentage(String gateId, int percentage) {
      return Mono.<Void>fromRunnable(() -> store.put(gateId, percentage));
    }

    @Override
    public Mono<Boolean> removeRolloutPercentage(String gateId) {
      return Mono.fromCallable(() -> store.remove(gateId) != null);
    }
  }
}
