package net.brightroom.endpointgate.spring.core.properties;

/**
 * Configuration for a single endpoint gate, including its enabled status, rollout percentage,
 * optional condition expression, and optional schedule.
 *
 * <p>Used as the value type for {@code endpoint-gate.gates} in configuration.
 *
 * <p>Configuration example in {@code application.yml}:
 *
 * <pre>{@code
 * endpoint-gate:
 *   gates:
 *     new-feature:
 *       enabled: true
 *       rollout: 50
 *       condition: "headers['X-Beta'] != null"
 *     christmas-sale:
 *       enabled: true
 *       schedule:
 *         start: "2026-12-25T00:00:00"
 *         end: "2027-01-05T23:59:59"
 *         timezone: "Asia/Tokyo"
 *     simple-feature:
 *       enabled: true
 * }</pre>
 */
public class GateProperties {

  private boolean enabled = true;
  private Integer rollout;
  private String condition = "";
  private ScheduleProperties schedule;

  /**
   * Returns whether this gate is enabled.
   *
   * @return {@code true} if the gate is enabled, {@code false} otherwise
   */
  public boolean enabled() {
    return enabled;
  }

  /**
   * Returns the rollout percentage for this gate (0–100), or {@code null} if not explicitly
   * configured.
   *
   * <p>{@code null} indicates that no rollout percentage was specified in configuration, allowing
   * fallback values to be applied by the caller.
   *
   * @return the rollout percentage, or {@code null} if not configured
   */
  public Integer rollout() {
    return rollout;
  }

  /**
   * Returns the condition expression for this gate, or an empty string if no condition is
   * configured.
   *
   * @return the condition expression, or empty string
   */
  public String condition() {
    return condition;
  }

  /**
   * Returns the schedule configuration for this gate, or {@code null} if no schedule is configured.
   *
   * @return the schedule configuration, or {@code null}
   */
  public ScheduleProperties schedule() {
    return schedule;
  }

  // for property binding
  void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  // for property binding
  void setRollout(int rollout) {
    if (rollout < 0 || rollout > 100) {
      throw new IllegalArgumentException("rollout must be between 0 and 100, but was: " + rollout);
    }
    this.rollout = rollout;
  }

  // for property binding
  void setCondition(String condition) {
    if (condition != null) {
      this.condition = condition;
    } else {
      this.condition = "";
    }
  }

  // for property binding
  void setSchedule(ScheduleProperties schedule) {
    this.schedule = schedule;
  }

  GateProperties() {}
}
