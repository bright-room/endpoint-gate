package net.brightroom.endpointgate.spring.core.properties;

import java.time.ZoneId;

/**
 * Global schedule configuration properties.
 *
 * <p>Configuration example in {@code application.yml}:
 *
 * <pre>{@code
 * endpoint-gate:
 *   schedule:
 *     default-timezone: "Asia/Tokyo"
 * }</pre>
 */
public class GlobalScheduleProperties {

  private ZoneId defaultTimezone;

  /**
   * Returns the default timezone for schedule evaluation, or {@code null} if the system default
   * timezone should be used.
   *
   * @return the default timezone, or {@code null}
   */
  public ZoneId defaultTimezone() {
    return defaultTimezone;
  }

  // for property binding
  void setDefaultTimezone(ZoneId defaultTimezone) {
    this.defaultTimezone = defaultTimezone;
  }

  GlobalScheduleProperties() {}
}
