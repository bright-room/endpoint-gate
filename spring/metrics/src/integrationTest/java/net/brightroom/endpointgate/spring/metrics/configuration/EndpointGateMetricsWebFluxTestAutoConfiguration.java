package net.brightroom.endpointgate.spring.metrics.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.webflux.autoconfigure.EndpointGateWebFluxAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.autoconfigure.EndpointGateMvcAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.autoconfigure.EndpointGateMvcInterceptorRegistrationAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Test auto-configuration for the spring-metrics module in a Reactive environment.
 *
 * <p>{@code @Import} is kept to make the dependency on the auto-configurations explicit and to
 * ensure they are loaded even if the {@code @EnableAutoConfiguration} scanning order changes.
 *
 * <p>Registers a {@link SimpleMeterRegistry} bean so that the metrics auto-configuration's {@code
 * ConditionalOnBean(MeterRegistry.class)} condition is satisfied.
 */
@Configuration
@EnableAutoConfiguration(
    exclude = {
      EndpointGateMvcAutoConfiguration.class,
      EndpointGateMvcInterceptorRegistrationAutoConfiguration.class
    })
@EnableConfigurationProperties(EndpointGateProperties.class)
@Import({EndpointGateAutoConfiguration.class, EndpointGateWebFluxAutoConfiguration.class})
public class EndpointGateMetricsWebFluxTestAutoConfiguration {

  /**
   * Registers a {@link SimpleMeterRegistry} for test metric collection.
   *
   * @return the test meter registry
   */
  @Bean
  MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }
}
