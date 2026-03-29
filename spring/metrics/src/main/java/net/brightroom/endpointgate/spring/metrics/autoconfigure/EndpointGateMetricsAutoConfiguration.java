package net.brightroom.endpointgate.spring.metrics.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.spring.metrics.evaluation.InstrumentedEndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.spring.metrics.evaluation.InstrumentedReactiveEndpointGateEvaluationPipeline;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * {@link AutoConfiguration Auto-configuration} for endpoint gate Micrometer metrics.
 *
 * <p>This configuration runs after the webmvc/webflux auto-configurations to ensure the original
 * {@link EndpointGateEvaluationPipeline} or {@link ReactiveEndpointGateEvaluationPipeline} bean is
 * already registered. It wraps the existing pipeline with an instrumented decorator that records
 * evaluation counters and timers.
 *
 * <p>The decorated pipeline is registered as {@code @Primary} so that interceptors and aspects
 * inject the instrumented version.
 *
 * <p>Activation requires a {@link MeterRegistry} bean to be present in the application context.
 */
@AutoConfiguration(
    afterName = {
      "net.brightroom.endpointgate.spring.webmvc.autoconfigure.EndpointGateMvcAutoConfiguration",
      "net.brightroom.endpointgate.spring.webflux.autoconfigure.EndpointGateWebFluxAutoConfiguration"
    })
@ConditionalOnBean(MeterRegistry.class)
public class EndpointGateMetricsAutoConfiguration {

  /** Creates a new {@link EndpointGateMetricsAutoConfiguration}. */
  EndpointGateMetricsAutoConfiguration() {}

  /** Servlet-specific metrics configuration. */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  static class ServletMetricsConfiguration {

    /**
     * Registers an instrumented evaluation pipeline that wraps the existing pipeline with
     * Micrometer metrics.
     *
     * @param pipeline the original evaluation pipeline
     * @param registry the meter registry
     * @return the instrumented pipeline
     */
    @Bean
    @Primary
    InstrumentedEndpointGateEvaluationPipeline instrumentedEvaluationPipeline(
        EndpointGateEvaluationPipeline pipeline, MeterRegistry registry) {
      return new InstrumentedEndpointGateEvaluationPipeline(pipeline, registry);
    }

    ServletMetricsConfiguration() {}
  }

  /** Reactive-specific metrics configuration. */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
  static class ReactiveMetricsConfiguration {

    /**
     * Registers an instrumented reactive evaluation pipeline that wraps the existing pipeline with
     * Micrometer metrics.
     *
     * @param pipeline the original reactive evaluation pipeline
     * @param registry the meter registry
     * @return the instrumented reactive pipeline
     */
    @Bean
    @Primary
    InstrumentedReactiveEndpointGateEvaluationPipeline instrumentedReactiveEvaluationPipeline(
        ReactiveEndpointGateEvaluationPipeline pipeline, MeterRegistry registry) {
      return new InstrumentedReactiveEndpointGateEvaluationPipeline(pipeline, registry);
    }

    ReactiveMetricsConfiguration() {}
  }
}
