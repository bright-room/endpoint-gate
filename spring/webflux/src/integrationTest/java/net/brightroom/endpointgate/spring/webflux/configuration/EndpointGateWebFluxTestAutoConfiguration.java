package net.brightroom.endpointgate.spring.webflux.configuration;

import net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.webflux.autoconfigure.EndpointGateWebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Test auto-configuration for the webflux module.
 *
 * <p>{@code @EnableAutoConfiguration} loads {@code EndpointGateAutoConfiguration} from the core
 * module via {@code
 * META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}. It also loads
 * the webflux auto-configuration through the same mechanism, so the {@code @Import} below results
 * in the webflux auto-configuration being registered twice. Spring handles this gracefully by
 * deduplicating bean definitions with the same name.
 *
 * <p>{@code @Import} is kept here to make the dependency on the webflux auto-configuration explicit
 * and to ensure it is loaded even if the {@code @EnableAutoConfiguration} scanning order changes in
 * future Spring Boot versions.
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(EndpointGateProperties.class)
@Import({EndpointGateAutoConfiguration.class, EndpointGateWebFluxAutoConfiguration.class})
public class EndpointGateWebFluxTestAutoConfiguration {}
