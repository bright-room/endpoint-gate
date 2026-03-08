package net.brightroom.endpointgate.spring.core.properties;

import java.util.HashMap;
import java.util.Map;
import net.brightroom.endpointgate.core.provider.Schedule;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for endpoint gate configuration.
 *
 * <p>These properties are used to define the enabled status and rollout percentage for specific
 * gates.
 *
 * <p>Configuration example in {@code application.yml}:
 *
 * <pre>{@code
 * endpoint-gate:
 *   gates:
 *     new-feature:
 *       enabled: true
 *       rollout: 50
 *     beta-feature:
 *       enabled: true
 *       rollout: 25
 *     simple-feature:
 *       enabled: true
 * }</pre>
 */
@ConfigurationProperties(prefix = "endpoint-gate")
public class EndpointGateProperties {

  private Map<String, GateProperties> gates = new HashMap<>();
  private ResponseProperties response = new ResponseProperties();
  private ConditionProperties condition = new ConditionProperties();
  private boolean defaultEnabled = false;

  /**
   * Returns a map of gate IDs and their enabled status, derived from {@code gates}.
   *
   * <p>This is a convenience view for components that only need enabled/disabled status (e.g.,
   * {@code EndpointGateProvider} initialization).
   *
   * @return an immutable map of gate IDs to their enabled states
   */
  public Map<String, Boolean> gateIds() {
    var result = new HashMap<String, Boolean>();
    gates.forEach((id, config) -> result.put(id, config.enabled()));
    return Map.copyOf(result);
  }

  /**
   * Returns a map of gate IDs and their rollout percentages, derived from {@code gates}.
   *
   * @return an immutable map of gate IDs to their rollout percentages
   */
  public Map<String, Integer> rolloutPercentages() {
    var result = new HashMap<String, Integer>();
    gates.forEach((id, config) -> result.put(id, config.rollout()));
    return Map.copyOf(result);
  }

  /**
   * Returns a map of gate IDs to their condition expressions. Gates without a condition (empty
   * string) are excluded.
   *
   * @return an immutable map of gate IDs to their condition expressions
   */
  public Map<String, String> conditions() {
    var result = new HashMap<String, String>();
    gates.forEach(
        (id, config) -> {
          if (!config.condition().isEmpty()) {
            result.put(id, config.condition());
          }
        });
    return Map.copyOf(result);
  }

  /**
   * Returns a map of gate IDs to their schedule value objects. Gates without a schedule are
   * excluded.
   *
   * @return an immutable map of gate IDs to their {@link Schedule}
   */
  public Map<String, Schedule> schedules() {
    var result = new HashMap<String, Schedule>();
    gates.forEach(
        (id, config) -> {
          if (config.schedule() != null) {
            result.put(id, config.schedule().toSchedule());
          }
        });
    return Map.copyOf(result);
  }

  /**
   * Returns the full gate configuration map.
   *
   * @return an immutable map of gate IDs to their {@link GateProperties}
   */
  public Map<String, GateProperties> gates() {
    return Map.copyOf(gates);
  }

  /**
   * Returns the response properties.
   *
   * @return the response properties
   */
  public ResponseProperties response() {
    return response;
  }

  /**
   * Returns the condition evaluation properties.
   *
   * @return the condition properties
   */
  public ConditionProperties condition() {
    return condition;
  }

  /**
   * Returns whether undefined endpoint gates are enabled by default.
   *
   * @return {@code true} if undefined gates are enabled (fail-open), {@code false} if disabled
   *     (fail-closed)
   */
  public boolean defaultEnabled() {
    return defaultEnabled;
  }

  // for property binding
  void setGates(Map<String, GateProperties> gates) {
    this.gates = gates;
  }

  // for property binding
  void setResponse(ResponseProperties response) {
    this.response = response;
  }

  // for property binding
  void setCondition(ConditionProperties condition) {
    this.condition = condition;
  }

  // for property binding
  void setDefaultEnabled(boolean defaultEnabled) {
    this.defaultEnabled = defaultEnabled;
  }

  EndpointGateProperties() {}
}
