package net.brightroom.endpointgate.spring.webmvc.autoconfigure;

import java.time.Clock;
import net.brightroom.endpointgate.core.condition.EndpointGateConditionEvaluator;
import net.brightroom.endpointgate.core.evaluation.ConditionEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.EnabledEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.core.evaluation.RolloutEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.ScheduleEvaluationStep;
import net.brightroom.endpointgate.core.provider.ConditionProvider;
import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import net.brightroom.endpointgate.core.provider.InMemoryConditionProvider;
import net.brightroom.endpointgate.core.provider.InMemoryEndpointGateProvider;
import net.brightroom.endpointgate.core.provider.InMemoryRolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.InMemoryScheduleProvider;
import net.brightroom.endpointgate.core.provider.RolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.ScheduleProvider;
import net.brightroom.endpointgate.core.rollout.DefaultRolloutStrategy;
import net.brightroom.endpointgate.core.rollout.RolloutStrategy;
import net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration;
import net.brightroom.endpointgate.spring.core.condition.SpelEndpointGateConditionEvaluator;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webmvc.context.RandomEndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webmvc.exception.EndpointGateExceptionHandler;
import net.brightroom.endpointgate.spring.webmvc.filter.EndpointGateHandlerFilterFunction;
import net.brightroom.endpointgate.spring.webmvc.interceptor.EndpointGateInterceptor;
import net.brightroom.endpointgate.spring.webmvc.resolution.AccessDeniedInterceptResolution;
import net.brightroom.endpointgate.spring.webmvc.resolution.AccessDeniedInterceptResolutionFactory;
import net.brightroom.endpointgate.spring.webmvc.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import net.brightroom.endpointgate.spring.webmvc.resolution.handlerfilter.AccessDeniedHandlerFilterResolutionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Spring MVC endpoint gate support.
 *
 * <p>Registers the core beans required for endpoint gate enforcement in servlet-based Spring MVC
 * applications, including {@link net.brightroom.endpointgate.core.provider.EndpointGateProvider},
 * {@link net.brightroom.endpointgate.spring.webmvc.interceptor.EndpointGateInterceptor}, {@link
 * net.brightroom.endpointgate.spring.webmvc.exception.EndpointGateExceptionHandler}, and related
 * resolution and rollout beans. Beans annotated with {@code @ConditionalOnMissingBean} can be
 * replaced by user-defined beans.
 */
@AutoConfiguration(after = EndpointGateAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class EndpointGateMvcAutoConfiguration {

  private final EndpointGateProperties endpointGateProperties;

  @Bean
  @ConditionalOnMissingBean(EndpointGateProvider.class)
  EndpointGateProvider endpointGateProvider() {
    return new InMemoryEndpointGateProvider(
        endpointGateProperties.gateIds(), endpointGateProperties.defaultEnabled());
  }

  @Bean
  @ConditionalOnMissingBean
  AccessDeniedInterceptResolution endpointGateAccessDeniedResponse() {
    return new AccessDeniedInterceptResolutionFactory().create(endpointGateProperties);
  }

  @Bean
  @ConditionalOnMissingBean
  RolloutStrategy rolloutStrategy() {
    return new DefaultRolloutStrategy();
  }

  @Bean
  @ConditionalOnMissingBean
  EndpointGateContextResolver endpointGateContextResolver() {
    return new RandomEndpointGateContextResolver();
  }

  @Bean
  @ConditionalOnMissingBean(RolloutPercentageProvider.class)
  RolloutPercentageProvider rolloutPercentageProvider() {
    return new InMemoryRolloutPercentageProvider(endpointGateProperties.rolloutPercentages());
  }

  @Bean
  @ConditionalOnMissingBean(ConditionProvider.class)
  ConditionProvider conditionProvider() {
    return new InMemoryConditionProvider(endpointGateProperties.conditions());
  }

  @Bean
  @ConditionalOnMissingBean
  EndpointGateConditionEvaluator endpointGateConditionEvaluator() {
    return new SpelEndpointGateConditionEvaluator(endpointGateProperties.condition().failOnError());
  }

  @Bean
  @ConditionalOnMissingBean
  Clock endpointGateClock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  @ConditionalOnMissingBean(ScheduleProvider.class)
  ScheduleProvider scheduleProvider() {
    return new InMemoryScheduleProvider(endpointGateProperties.schedules());
  }

  @Bean
  @ConditionalOnMissingBean(EnabledEvaluationStep.class)
  EnabledEvaluationStep enabledEvaluationStep(EndpointGateProvider endpointGateProvider) {
    return new EnabledEvaluationStep(endpointGateProvider);
  }

  @Bean
  @ConditionalOnMissingBean(ScheduleEvaluationStep.class)
  ScheduleEvaluationStep scheduleEvaluationStep(ScheduleProvider scheduleProvider, Clock clock) {
    return new ScheduleEvaluationStep(scheduleProvider, clock);
  }

  @Bean
  @ConditionalOnMissingBean(ConditionEvaluationStep.class)
  ConditionEvaluationStep conditionEvaluationStep(
      EndpointGateConditionEvaluator conditionEvaluator) {
    return new ConditionEvaluationStep(conditionEvaluator);
  }

  @Bean
  @ConditionalOnMissingBean(RolloutEvaluationStep.class)
  RolloutEvaluationStep rolloutEvaluationStep(RolloutStrategy rolloutStrategy) {
    return new RolloutEvaluationStep(rolloutStrategy);
  }

  @Bean
  @ConditionalOnMissingBean
  EndpointGateEvaluationPipeline endpointGateEvaluationPipeline(
      EnabledEvaluationStep enabledEvaluationStep,
      ScheduleEvaluationStep scheduleEvaluationStep,
      ConditionEvaluationStep conditionEvaluationStep,
      RolloutEvaluationStep rolloutEvaluationStep) {
    return new EndpointGateEvaluationPipeline(
        enabledEvaluationStep,
        scheduleEvaluationStep,
        conditionEvaluationStep,
        rolloutEvaluationStep);
  }

  @Bean
  EndpointGateInterceptor endpointGateInterceptor(
      EndpointGateEvaluationPipeline pipeline,
      RolloutPercentageProvider rolloutPercentageProvider,
      ConditionProvider conditionProvider,
      EndpointGateContextResolver contextResolver) {
    return new EndpointGateInterceptor(
        pipeline, rolloutPercentageProvider, conditionProvider, contextResolver);
  }

  @Bean
  EndpointGateExceptionHandler endpointGateExceptionHandler(
      AccessDeniedInterceptResolution accessDeniedInterceptResolution) {
    return new EndpointGateExceptionHandler(accessDeniedInterceptResolution);
  }

  @Bean
  @ConditionalOnMissingBean(AccessDeniedHandlerFilterResolution.class)
  AccessDeniedHandlerFilterResolution accessDeniedHandlerFilterResolution() {
    return new AccessDeniedHandlerFilterResolutionFactory().create(endpointGateProperties);
  }

  @Bean
  @ConditionalOnMissingBean
  EndpointGateHandlerFilterFunction endpointGateHandlerFilterFunction(
      EndpointGateEvaluationPipeline pipeline,
      AccessDeniedHandlerFilterResolution accessDeniedHandlerFilterResolution,
      RolloutPercentageProvider rolloutPercentageProvider,
      ConditionProvider conditionProvider,
      EndpointGateContextResolver contextResolver) {
    return new EndpointGateHandlerFilterFunction(
        pipeline,
        accessDeniedHandlerFilterResolution,
        rolloutPercentageProvider,
        conditionProvider,
        contextResolver);
  }

  /**
   * Creates a new {@link EndpointGateMvcAutoConfiguration}.
   *
   * @param endpointGateProperties the endpoint gate configuration properties; must not be null
   */
  EndpointGateMvcAutoConfiguration(EndpointGateProperties endpointGateProperties) {
    this.endpointGateProperties = endpointGateProperties;
  }
}
