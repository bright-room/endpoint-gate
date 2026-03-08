package net.brightroom.endpointgate.spring.core.autoconfigure;

import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * {@link AutoConfiguration Auto-configuration} for endpoint gate support.
 *
 * <p>This configuration enables {@link EndpointGateProperties} to be populated from the application
 * environment (e.g., {@code application.yml}).
 */
@AutoConfiguration
@EnableConfigurationProperties(EndpointGateProperties.class)
public class EndpointGateAutoConfiguration {

  /** Default constructor. */
  EndpointGateAutoConfiguration() {}
}
