package net.brightroom.endpointgate.spring.actuator.configuration;

import net.brightroom.endpointgate.spring.actuator.autoconfigure.EndpointGateActuatorAutoConfiguration;
import net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Test auto-configuration for the actuator module integration tests.
 *
 * <p>Explicitly imports the core and actuator auto-configurations to ensure they are loaded even
 * when the auto-configuration scanning order changes.
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(EndpointGateProperties.class)
@Import({EndpointGateAutoConfiguration.class, EndpointGateActuatorAutoConfiguration.class})
public class EndpointGateActuatorTestAutoConfiguration {}
