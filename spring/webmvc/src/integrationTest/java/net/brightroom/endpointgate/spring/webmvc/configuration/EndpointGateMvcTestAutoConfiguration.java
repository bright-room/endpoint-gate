package net.brightroom.endpointgate.spring.webmvc.configuration;

import net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.webmvc.autoconfigure.EndpointGateMvcAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.autoconfigure.EndpointGateMvcInterceptorRegistrationAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Test auto-configuration for the spring-webmvc module.
 *
 * <p>{@code @EnableAutoConfiguration} loads {@code EndpointGateAutoConfiguration} from the
 * spring-core module via {@code
 * META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}. It also loads
 * the spring-webmvc auto-configurations through the same mechanism, so the {@code @Import} below
 * results in the spring-webmvc auto-configurations being registered twice. Spring handles this
 * gracefully by deduplicating bean definitions with the same name.
 *
 * <p>{@code @Import} is kept here to make the dependency on the spring-webmvc auto-configurations
 * explicit and to ensure they are loaded even if the {@code @EnableAutoConfiguration} scanning
 * order changes in future Spring Boot versions.
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(EndpointGateProperties.class)
@Import({
  EndpointGateAutoConfiguration.class,
  EndpointGateMvcAutoConfiguration.class,
  EndpointGateMvcInterceptorRegistrationAutoConfiguration.class
})
public class EndpointGateMvcTestAutoConfiguration {}
