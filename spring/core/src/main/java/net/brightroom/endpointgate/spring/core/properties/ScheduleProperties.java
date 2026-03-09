package net.brightroom.endpointgate.spring.core.properties;

import java.time.LocalDateTime;
import java.time.ZoneId;
import net.brightroom.endpointgate.core.provider.Schedule;
import org.jspecify.annotations.Nullable;

/**
 * Configuration for the schedule window of a single endpoint gate.
 *
 * <p>Defines an optional {@code start} and {@code end} time (as {@link LocalDateTime}) and an
 * optional {@code timezone}. Use {@link #toSchedule(ZoneId)} to obtain the {@link Schedule} SPI
 * value object which provides the {@code isActive(Instant)} evaluation logic.
 *
 * <p>Configuration example in {@code application.yml}:
 *
 * <pre>{@code
 * endpoint-gate:
 *   gates:
 *     christmas-sale:
 *       enabled: true
 *       schedule:
 *         start: "2026-12-25T00:00:00"
 *         end: "2027-01-05T23:59:59"
 *         timezone: "Asia/Tokyo"
 * }</pre>
 *
 * <ul>
 *   <li>{@code start} only — the gate is active from {@code start} onward
 *   <li>{@code end} only — the gate is active until {@code end}
 *   <li>both — the gate is active between {@code start} and {@code end} (inclusive)
 *   <li>neither — the gate is always active (equivalent to no schedule)
 *   <li>{@code timezone} omitted — the global default timezone ({@code
 *       endpoint-gate.schedule.default-timezone}) is used if configured, otherwise the system
 *       default timezone
 * </ul>
 */
public class ScheduleProperties {

  private LocalDateTime start;
  private LocalDateTime end;
  private ZoneId timezone;

  /**
   * Converts this property binding object to the SPI value type, using the given default timezone
   * when this schedule has no timezone configured.
   *
   * @param defaultTimezone the fallback timezone, or {@code null} to use the system default
   * @return a new {@link Schedule} with the resolved timezone
   */
  Schedule toSchedule(@Nullable ZoneId defaultTimezone) {
    ZoneId resolvedTimezone = timezone;
    if (resolvedTimezone == null) {
      resolvedTimezone = defaultTimezone;
    }
    return new Schedule(start, end, resolvedTimezone);
  }

  /**
   * Returns the schedule start time, or {@code null} if no start restriction is configured.
   *
   * @return the start time, or {@code null}
   */
  public LocalDateTime start() {
    return start;
  }

  /**
   * Returns the schedule end time, or {@code null} if no end restriction is configured.
   *
   * @return the end time, or {@code null}
   */
  public LocalDateTime end() {
    return end;
  }

  /**
   * Returns the timezone used to evaluate start/end times, or {@code null} if the system default
   * timezone should be used.
   *
   * @return the timezone, or {@code null}
   */
  public ZoneId timezone() {
    return timezone;
  }

  // for property binding
  void setStart(LocalDateTime start) {
    if (start == null) {
      this.start = null;
      return;
    }
    if (this.end != null) {
      if (start.isAfter(this.end)) {
        throw new IllegalArgumentException(
            String.format(
                "schedule.start must not be after schedule.end, but start=%s end=%s",
                start, this.end));
      }
    }
    this.start = start;
  }

  // for property binding
  void setEnd(LocalDateTime end) {
    if (end == null) {
      this.end = null;
      return;
    }
    if (this.start != null) {
      if (end.isBefore(this.start)) {
        throw new IllegalArgumentException(
            String.format(
                "schedule.end must not be before schedule.start, but end=%s start=%s",
                end, this.start));
      }
    }
    this.end = end;
  }

  // for property binding
  void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }

  ScheduleProperties() {}
}
