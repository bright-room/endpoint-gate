package net.brightroom.endpointgate.spring.actuator.health;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the endpoint gate health indicator.
 *
 * <p>Configuration example in {@code application.yml}:
 *
 * <pre>{@code
 * management:
 *   health:
 *     endpoint-gate:
 *       timeout: 5s
 * }</pre>
 */
@ConfigurationProperties(prefix = "management.health.endpoint-gate")
public class EndpointGateHealthProperties {

  /**
   * Maximum time to wait for the endpoint gate provider to respond during a health check. When the
   * provider does not respond within this duration, the health status is reported as {@code DOWN}.
   * If not set, there is no timeout.
   */
  private Duration timeout;

  /**
   * Returns the health check timeout.
   *
   * @return the timeout duration, or {@code null} if no timeout is configured
   */
  public Duration timeout() {
    return timeout;
  }

  // for property binding
  void setTimeout(Duration timeout) {
    this.timeout = timeout;
  }

  EndpointGateHealthProperties() {}
}
