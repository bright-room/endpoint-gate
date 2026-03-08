package net.brightroom.endpointgate.spring.webflux.autoconfigure;

import java.time.Clock;
import net.brightroom.endpointgate.core.condition.EndpointGateConditionEvaluator;
import net.brightroom.endpointgate.reactive.core.condition.ReactiveEndpointGateConditionEvaluator;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveConditionEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEnabledEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveRolloutEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveScheduleEvaluationStep;
import net.brightroom.endpointgate.reactive.core.provider.InMemoryReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.InMemoryReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.InMemoryReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.InMemoryReactiveScheduleProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveScheduleProvider;
import net.brightroom.endpointgate.reactive.core.rollout.DefaultReactiveRolloutStrategy;
import net.brightroom.endpointgate.reactive.core.rollout.ReactiveRolloutStrategy;
import net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration;
import net.brightroom.endpointgate.spring.core.condition.SpelEndpointGateConditionEvaluator;
import net.brightroom.endpointgate.spring.core.condition.SpelReactiveEndpointGateConditionEvaluator;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.webflux.aspect.EndpointGateAspect;
import net.brightroom.endpointgate.spring.webflux.context.RandomReactiveEndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webflux.context.ReactiveEndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webflux.exception.EndpointGateExceptionHandler;
import net.brightroom.endpointgate.spring.webflux.filter.EndpointGateHandlerFilterFunction;
import net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler.AccessDeniedExceptionHandlerResolution;
import net.brightroom.endpointgate.spring.webflux.resolution.exceptionhandler.AccessDeniedExceptionHandlerResolutionFactory;
import net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter.AccessDeniedHandlerFilterResolutionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

/**
 * Auto-configuration for the Spring WebFlux endpoint gate integration.
 *
 * <p>Registers the following beans when running in a reactive web application:
 *
 * <ul>
 *   <li>{@link ReactiveEndpointGateProvider} — in-memory provider backed by {@code
 *       endpoint-gate.gates} config (conditional on missing bean)
 *   <li>{@link EndpointGateAspect} — AOP aspect for annotation-based controllers (conditional on
 *       missing bean)
 *   <li>{@link EndpointGateHandlerFilterFunction} — filter factory for functional endpoints
 *       (conditional on missing bean)
 *   <li>{@link WebFilter} — propagates {@link ServerWebExchange} into the Reactor context
 * </ul>
 */
@AutoConfiguration(after = EndpointGateAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class EndpointGateWebFluxAutoConfiguration {

  private final EndpointGateProperties endpointGateProperties;

  @Bean
  @ConditionalOnMissingBean(ReactiveEndpointGateProvider.class)
  ReactiveEndpointGateProvider reactiveEndpointGateProvider() {
    return new InMemoryReactiveEndpointGateProvider(
        endpointGateProperties.gateIds(), endpointGateProperties.defaultEnabled());
  }

  @Bean
  @ConditionalOnMissingBean
  AccessDeniedExceptionHandlerResolution accessDeniedExceptionHandlerResolution() {
    return new AccessDeniedExceptionHandlerResolutionFactory().create(endpointGateProperties);
  }

  @Bean
  EndpointGateExceptionHandler endpointGateExceptionHandler(
      AccessDeniedExceptionHandlerResolution accessDeniedExceptionHandlerResolution) {
    return new EndpointGateExceptionHandler(accessDeniedExceptionHandlerResolution);
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveRolloutStrategy.class)
  ReactiveRolloutStrategy reactiveRolloutStrategy() {
    return new DefaultReactiveRolloutStrategy();
  }

  @Bean
  @ConditionalOnMissingBean
  ReactiveEndpointGateContextResolver reactiveEndpointGateContextResolver() {
    return new RandomReactiveEndpointGateContextResolver();
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveRolloutPercentageProvider.class)
  ReactiveRolloutPercentageProvider reactiveRolloutPercentageProvider() {
    return new InMemoryReactiveRolloutPercentageProvider(
        endpointGateProperties.rolloutPercentages());
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveConditionProvider.class)
  ReactiveConditionProvider reactiveConditionProvider() {
    return new InMemoryReactiveConditionProvider(endpointGateProperties.conditions());
  }

  @Bean
  @ConditionalOnMissingBean
  Clock endpointGateClock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveScheduleProvider.class)
  ReactiveScheduleProvider reactiveScheduleProvider() {
    return new InMemoryReactiveScheduleProvider(endpointGateProperties.schedules());
  }

  @Bean
  @ConditionalOnMissingBean
  EndpointGateConditionEvaluator endpointGateConditionEvaluator() {
    return new SpelEndpointGateConditionEvaluator(endpointGateProperties.condition().failOnError());
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveEndpointGateConditionEvaluator.class)
  ReactiveEndpointGateConditionEvaluator reactiveEndpointGateConditionEvaluator(
      EndpointGateConditionEvaluator conditionEvaluator) {
    return new SpelReactiveEndpointGateConditionEvaluator(conditionEvaluator);
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveEnabledEvaluationStep.class)
  ReactiveEnabledEvaluationStep reactiveEnabledEvaluationStep(
      ReactiveEndpointGateProvider reactiveEndpointGateProvider) {
    return new ReactiveEnabledEvaluationStep(reactiveEndpointGateProvider);
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveScheduleEvaluationStep.class)
  ReactiveScheduleEvaluationStep reactiveScheduleEvaluationStep(
      ReactiveScheduleProvider reactiveScheduleProvider, Clock clock) {
    return new ReactiveScheduleEvaluationStep(reactiveScheduleProvider, clock);
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveConditionEvaluationStep.class)
  ReactiveConditionEvaluationStep reactiveConditionEvaluationStep(
      ReactiveEndpointGateConditionEvaluator conditionEvaluator) {
    return new ReactiveConditionEvaluationStep(conditionEvaluator);
  }

  @Bean
  @ConditionalOnMissingBean(ReactiveRolloutEvaluationStep.class)
  ReactiveRolloutEvaluationStep reactiveRolloutEvaluationStep(
      ReactiveRolloutStrategy reactiveRolloutStrategy) {
    return new ReactiveRolloutEvaluationStep(reactiveRolloutStrategy);
  }

  @Bean
  @ConditionalOnMissingBean
  ReactiveEndpointGateEvaluationPipeline reactiveEndpointGateEvaluationPipeline(
      ReactiveEnabledEvaluationStep enabledStep,
      ReactiveScheduleEvaluationStep scheduleStep,
      ReactiveConditionEvaluationStep conditionStep,
      ReactiveRolloutEvaluationStep rolloutStep) {
    return new ReactiveEndpointGateEvaluationPipeline(
        enabledStep, scheduleStep, conditionStep, rolloutStep);
  }

  /**
   * Propagates {@link ServerWebExchange} into the Reactor context so that {@link
   * EndpointGateAspect} can access it via {@code Mono.deferContextual} during rollout percentage
   * checks.
   *
   * <p>Spring WebFlux does not automatically add {@link ServerWebExchange} to the Reactor context,
   * so this filter bridges the gap between the servlet-style exchange object and the reactive
   * context, enabling the aspect to resolve the request for sticky rollout without requiring
   * constructor injection of the exchange.
   */
  @Bean
  WebFilter endpointGateServerWebExchangeContextFilter() {
    return (exchange, chain) ->
        chain.filter(exchange).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange));
  }

  @Bean
  @ConditionalOnMissingBean
  EndpointGateAspect endpointGateAspect(
      ReactiveEndpointGateEvaluationPipeline pipeline,
      ReactiveEndpointGateContextResolver contextResolver,
      ReactiveRolloutPercentageProvider reactiveRolloutPercentageProvider,
      ReactiveConditionProvider reactiveConditionProvider) {
    return new EndpointGateAspect(
        pipeline, contextResolver, reactiveRolloutPercentageProvider, reactiveConditionProvider);
  }

  @Bean
  @ConditionalOnMissingBean(AccessDeniedHandlerFilterResolution.class)
  AccessDeniedHandlerFilterResolution accessDeniedHandlerFilterResolution() {
    return new AccessDeniedHandlerFilterResolutionFactory().create(endpointGateProperties);
  }

  @Bean
  @ConditionalOnMissingBean
  EndpointGateHandlerFilterFunction endpointGateHandlerFilterFunction(
      ReactiveEndpointGateEvaluationPipeline pipeline,
      AccessDeniedHandlerFilterResolution accessDeniedHandlerFilterResolution,
      ReactiveRolloutPercentageProvider reactiveRolloutPercentageProvider,
      ReactiveConditionProvider reactiveConditionProvider,
      ReactiveEndpointGateContextResolver contextResolver) {
    return new EndpointGateHandlerFilterFunction(
        pipeline,
        accessDeniedHandlerFilterResolution,
        reactiveRolloutPercentageProvider,
        reactiveConditionProvider,
        contextResolver);
  }

  /**
   * Creates a new {@code EndpointGateWebFluxAutoConfiguration}.
   *
   * @param endpointGateProperties the endpoint gate configuration properties
   */
  EndpointGateWebFluxAutoConfiguration(EndpointGateProperties endpointGateProperties) {
    this.endpointGateProperties = endpointGateProperties;
  }
}
