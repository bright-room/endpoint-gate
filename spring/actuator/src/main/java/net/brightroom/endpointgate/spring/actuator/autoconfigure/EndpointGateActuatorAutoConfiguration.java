package net.brightroom.endpointgate.spring.actuator.autoconfigure;

import java.time.Clock;
import java.util.List;
import net.brightroom.endpointgate.core.provider.ConditionProvider;
import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import net.brightroom.endpointgate.core.provider.MutableConditionProvider;
import net.brightroom.endpointgate.core.provider.MutableEndpointGateProvider;
import net.brightroom.endpointgate.core.provider.MutableInMemoryConditionProvider;
import net.brightroom.endpointgate.core.provider.MutableInMemoryEndpointGateProvider;
import net.brightroom.endpointgate.core.provider.MutableInMemoryRolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.MutableInMemoryScheduleProvider;
import net.brightroom.endpointgate.core.provider.MutableRolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.MutableScheduleProvider;
import net.brightroom.endpointgate.core.provider.RolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.ScheduleProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableInMemoryReactiveScheduleProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.MutableReactiveScheduleProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveScheduleProvider;
import net.brightroom.endpointgate.spring.actuator.endpoint.EndpointGateEndpoint;
import net.brightroom.endpointgate.spring.actuator.endpoint.ReactiveEndpointGateEndpoint;
import net.brightroom.endpointgate.spring.actuator.health.EndpointGateHealthIndicator;
import net.brightroom.endpointgate.spring.actuator.health.EndpointGateHealthProperties;
import net.brightroom.endpointgate.spring.actuator.health.HealthDetailsContributor;
import net.brightroom.endpointgate.spring.actuator.health.ReactiveEndpointGateHealthIndicator;
import net.brightroom.endpointgate.spring.actuator.health.ReactiveHealthDetailsContributor;
import net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link AutoConfiguration Auto-configuration} for endpoint gate actuator support.
 *
 * <p>This configuration runs after {@link EndpointGateAutoConfiguration} (which binds {@link
 * EndpointGateProperties}) and before the webmvc/webflux auto-configurations. The ordering ensures
 * that the mutable provider is registered before those modules evaluate their own {@code
 * ConditionalOnMissingBean} conditions, preventing duplicate provider registration.
 *
 * <p>Provider registration is split by web application type:
 *
 * <ul>
 *   <li><b>Servlet:</b> Registers {@link MutableInMemoryEndpointGateProvider} and {@link
 *       EndpointGateEndpoint}
 *   <li><b>Reactive:</b> Registers {@link MutableInMemoryReactiveEndpointGateProvider} and {@link
 *       ReactiveEndpointGateEndpoint}
 * </ul>
 */
@AutoConfiguration(
    after = EndpointGateAutoConfiguration.class,
    beforeName = {
      "net.brightroom.endpointgate.spring.webmvc.autoconfigure.EndpointGateMvcAutoConfiguration",
      "net.brightroom.endpointgate.spring.webflux.autoconfigure.EndpointGateWebFluxAutoConfiguration"
    })
@EnableConfigurationProperties(EndpointGateHealthProperties.class)
public class EndpointGateActuatorAutoConfiguration {

  /** Creates a new {@link EndpointGateActuatorAutoConfiguration}. */
  EndpointGateActuatorAutoConfiguration() {}

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  static class ServletConfiguration {

    private final EndpointGateProperties endpointGateProperties;
    private final EndpointGateHealthProperties endpointGateHealthProperties;

    /**
     * Registers a {@link MutableInMemoryEndpointGateProvider} bean when no other {@link
     * EndpointGateProvider} bean is already present.
     *
     * @return the mutable in-memory provider initialized from {@link EndpointGateProperties}
     */
    @Bean
    @ConditionalOnMissingBean(EndpointGateProvider.class)
    MutableInMemoryEndpointGateProvider mutableEndpointGateProvider() {
      return new MutableInMemoryEndpointGateProvider(
          endpointGateProperties.gateIds(), endpointGateProperties.defaultEnabled());
    }

    /**
     * Registers a {@link MutableInMemoryRolloutPercentageProvider} bean when no other {@link
     * RolloutPercentageProvider} bean is already present.
     *
     * @return the mutable in-memory rollout percentage provider initialized from {@link
     *     EndpointGateProperties}
     */
    @Bean
    @ConditionalOnMissingBean(RolloutPercentageProvider.class)
    MutableInMemoryRolloutPercentageProvider mutableRolloutPercentageProvider() {
      return new MutableInMemoryRolloutPercentageProvider(
          endpointGateProperties.rolloutPercentages());
    }

    /**
     * Registers a {@link MutableInMemoryScheduleProvider} bean when no other {@link
     * ScheduleProvider} bean is already present.
     *
     * @return the mutable in-memory schedule provider initialized from {@link
     *     EndpointGateProperties}
     */
    @Bean
    @ConditionalOnMissingBean(ScheduleProvider.class)
    MutableInMemoryScheduleProvider scheduleProvider() {
      return new MutableInMemoryScheduleProvider(endpointGateProperties.schedules());
    }

    /**
     * Registers a {@link MutableInMemoryConditionProvider} bean when no other {@link
     * ConditionProvider} bean is already present.
     *
     * @return the mutable in-memory condition provider initialized from {@link
     *     EndpointGateProperties}
     */
    @Bean
    @ConditionalOnMissingBean(ConditionProvider.class)
    MutableInMemoryConditionProvider mutableConditionProvider() {
      return new MutableInMemoryConditionProvider(endpointGateProperties.conditions());
    }

    /**
     * Registers a {@link Clock} bean when no other {@link Clock} bean is already present.
     *
     * @return the system default clock
     */
    @Bean
    @ConditionalOnMissingBean
    Clock endpointGateClock() {
      return Clock.systemDefaultZone();
    }

    /**
     * Registers the {@link EndpointGateHealthIndicator} bean when the {@code endpointGate} health
     * indicator is enabled.
     *
     * @param provider the endpoint gate provider
     * @param contributors the list of {@link HealthDetailsContributor} beans; may be empty
     * @return the endpoint gate health indicator
     */
    @Bean
    @ConditionalOnEnabledHealthIndicator("endpointGate")
    EndpointGateHealthIndicator endpointGateHealthIndicator(
        EndpointGateProvider provider, List<HealthDetailsContributor> contributors) {
      return new EndpointGateHealthIndicator(
          provider, endpointGateProperties, endpointGateHealthProperties.timeout(), contributors);
    }

    /**
     * Registers the {@link EndpointGateEndpoint} bean when a {@link MutableEndpointGateProvider}
     * bean is present.
     *
     * @param provider the mutable endpoint gate provider
     * @param rolloutProvider the mutable rollout percentage provider
     * @param conditionProvider the mutable condition provider
     * @param scheduleProvider the schedule provider
     * @param eventPublisher the publisher used to broadcast gate change events
     * @param clock the clock used for schedule evaluation
     * @return the endpoint gate actuator endpoint
     */
    @Bean
    @ConditionalOnBean(MutableEndpointGateProvider.class)
    EndpointGateEndpoint endpointGateEndpoint(
        MutableEndpointGateProvider provider,
        MutableRolloutPercentageProvider rolloutProvider,
        MutableConditionProvider conditionProvider,
        MutableScheduleProvider scheduleProvider,
        ApplicationEventPublisher eventPublisher,
        Clock clock) {
      return new EndpointGateEndpoint(
          provider,
          rolloutProvider,
          conditionProvider,
          scheduleProvider,
          endpointGateProperties.defaultEnabled(),
          eventPublisher,
          clock);
    }

    ServletConfiguration(
        EndpointGateProperties endpointGateProperties,
        EndpointGateHealthProperties endpointGateHealthProperties) {
      this.endpointGateProperties = endpointGateProperties;
      this.endpointGateHealthProperties = endpointGateHealthProperties;
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
  static class ReactiveConfiguration {

    private final EndpointGateProperties endpointGateProperties;
    private final EndpointGateHealthProperties endpointGateHealthProperties;

    /**
     * Registers a {@link MutableInMemoryReactiveEndpointGateProvider} bean when no other {@link
     * ReactiveEndpointGateProvider} bean is already present.
     *
     * @return the mutable in-memory reactive provider initialized from {@link
     *     EndpointGateProperties}
     */
    @Bean
    @ConditionalOnMissingBean(ReactiveEndpointGateProvider.class)
    MutableInMemoryReactiveEndpointGateProvider mutableReactiveEndpointGateProvider() {
      return new MutableInMemoryReactiveEndpointGateProvider(
          endpointGateProperties.gateIds(), endpointGateProperties.defaultEnabled());
    }

    /**
     * Registers a {@link MutableInMemoryReactiveRolloutPercentageProvider} bean when no other
     * {@link ReactiveRolloutPercentageProvider} bean is already present.
     *
     * @return the mutable in-memory reactive rollout percentage provider initialized from {@link
     *     EndpointGateProperties}
     */
    @Bean
    @ConditionalOnMissingBean(ReactiveRolloutPercentageProvider.class)
    MutableInMemoryReactiveRolloutPercentageProvider mutableReactiveRolloutPercentageProvider() {
      return new MutableInMemoryReactiveRolloutPercentageProvider(
          endpointGateProperties.rolloutPercentages());
    }

    /**
     * Registers a {@link MutableInMemoryReactiveScheduleProvider} bean when no other {@link
     * ReactiveScheduleProvider} bean is already present.
     *
     * @return the mutable in-memory reactive schedule provider initialized from {@link
     *     EndpointGateProperties}
     */
    @Bean
    @ConditionalOnMissingBean(ReactiveScheduleProvider.class)
    MutableInMemoryReactiveScheduleProvider reactiveScheduleProvider() {
      return new MutableInMemoryReactiveScheduleProvider(endpointGateProperties.schedules());
    }

    /**
     * Registers a {@link MutableInMemoryReactiveConditionProvider} bean when no other {@link
     * ReactiveConditionProvider} bean is already present.
     *
     * @return the mutable in-memory reactive condition provider initialized from {@link
     *     EndpointGateProperties}
     */
    @Bean
    @ConditionalOnMissingBean(ReactiveConditionProvider.class)
    MutableInMemoryReactiveConditionProvider mutableReactiveConditionProvider() {
      return new MutableInMemoryReactiveConditionProvider(endpointGateProperties.conditions());
    }

    /**
     * Registers a {@link Clock} bean when no other {@link Clock} bean is already present.
     *
     * @return the system default clock
     */
    @Bean
    @ConditionalOnMissingBean
    Clock endpointGateClock() {
      return Clock.systemDefaultZone();
    }

    /**
     * Registers the {@link ReactiveEndpointGateHealthIndicator} bean when the {@code endpointGate}
     * health indicator is enabled.
     *
     * @param provider the reactive endpoint gate provider
     * @param contributors the list of {@link ReactiveHealthDetailsContributor} beans; may be empty
     * @return the reactive endpoint gate health indicator
     */
    @Bean
    @ConditionalOnEnabledHealthIndicator("endpointGate")
    ReactiveEndpointGateHealthIndicator endpointGateHealthIndicator(
        ReactiveEndpointGateProvider provider,
        List<ReactiveHealthDetailsContributor> contributors) {
      return new ReactiveEndpointGateHealthIndicator(
          provider, endpointGateProperties, endpointGateHealthProperties.timeout(), contributors);
    }

    /**
     * Registers the {@link ReactiveEndpointGateEndpoint} bean when a {@link
     * MutableReactiveEndpointGateProvider} bean is present.
     *
     * @param provider the mutable reactive endpoint gate provider
     * @param rolloutProvider the mutable reactive rollout percentage provider
     * @param reactiveConditionProvider the mutable reactive condition provider
     * @param reactiveScheduleProvider the reactive schedule provider
     * @param eventPublisher the publisher used to broadcast gate change events
     * @param clock the clock used for schedule evaluation
     * @return the reactive endpoint gate actuator endpoint
     */
    @Bean
    @ConditionalOnBean(MutableReactiveEndpointGateProvider.class)
    ReactiveEndpointGateEndpoint reactiveEndpointGateEndpoint(
        MutableReactiveEndpointGateProvider provider,
        MutableReactiveRolloutPercentageProvider rolloutProvider,
        MutableReactiveConditionProvider reactiveConditionProvider,
        MutableReactiveScheduleProvider reactiveScheduleProvider,
        ApplicationEventPublisher eventPublisher,
        Clock clock) {
      return new ReactiveEndpointGateEndpoint(
          provider,
          rolloutProvider,
          reactiveConditionProvider,
          reactiveScheduleProvider,
          endpointGateProperties.defaultEnabled(),
          eventPublisher,
          clock);
    }

    ReactiveConfiguration(
        EndpointGateProperties endpointGateProperties,
        EndpointGateHealthProperties endpointGateHealthProperties) {
      this.endpointGateProperties = endpointGateProperties;
      this.endpointGateHealthProperties = endpointGateHealthProperties;
    }
  }
}
