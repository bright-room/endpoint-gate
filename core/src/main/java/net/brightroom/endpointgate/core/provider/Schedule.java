package net.brightroom.endpointgate.core.provider;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.jspecify.annotations.Nullable;

/**
 * An immutable value object representing the schedule window of an endpoint gate.
 *
 * <p>Defines an optional {@code start} and {@code end} time (as {@link LocalDateTime}) and an
 * optional {@code timezone}. When {@link #isActive(Instant)} is called, the instant is converted to
 * the configured timezone (or system default if absent) and compared against the window.
 *
 * <p>This is the SPI return type for {@link ScheduleProvider}. Custom provider implementations
 * should create instances directly:
 *
 * <pre>{@code
 * return Optional.of(new Schedule(startTime, endTime, ZoneId.of("Asia/Tokyo")));
 * }</pre>
 *
 * <ul>
 *   <li>{@code start} only — the gate is active from {@code start} onward
 *   <li>{@code end} only — the gate is active until {@code end}
 *   <li>both — the gate is active between {@code start} and {@code end} (inclusive)
 *   <li>neither — the gate is always active (equivalent to no schedule)
 *   <li>{@code timezone} omitted — system default timezone is used
 * </ul>
 *
 * @param start the schedule start time, or {@code null} if no start restriction is configured
 * @param end the schedule end time, or {@code null} if no end restriction is configured
 * @param timezone the timezone used to evaluate start/end times, or {@code null} if the system
 *     default timezone should be used
 */
public record Schedule(
    @Nullable LocalDateTime start, @Nullable LocalDateTime end, @Nullable ZoneId timezone) {

  /** Validates that start is not after an end. */
  public Schedule {
    if (start != null) {
      if (end != null) {
        if (start.isAfter(end)) {
          throw new IllegalArgumentException(
              String.format(
                  "Schedule start must not be after end, but start=%s end=%s", start, end));
        }
      }
    }
  }

  /**
   * Returns whether the schedule window is active at the given instant.
   *
   * @param now the current instant to check; must not be null
   * @return {@code true} if {@code now} falls within the configured window, {@code false} otherwise
   */
  public boolean isActive(Instant now) {
    ZoneId zone = resolveZone();
    LocalDateTime localNow = now.atZone(zone).toLocalDateTime();
    if (start != null) {
      if (localNow.isBefore(start)) {
        return false;
      }
    }
    if (end != null) {
      return !localNow.isAfter(end);
    }
    return true;
  }

  /**
   * Returns the retry-after {@link Instant} for this schedule, or {@code null} if not applicable.
   *
   * <p>Returns the {@link #start()} instant if {@code start} is non-null and in the future relative
   * to the given {@code clock}. Returns {@code null} if {@code start} is null, or if {@code start}
   * is already in the past (to avoid sending a stale retry-after hint to clients).
   *
   * @param clock the clock used to determine the current time
   * @return the retry-after instant, or {@code null}
   */
  public @Nullable Instant retryAfterInstant(Clock clock) {
    if (start == null) {
      return null;
    }
    ZoneId zone = resolveZone();
    ZonedDateTime zonedStart = start.atZone(zone);
    Instant startInstant = zonedStart.toInstant();
    Instant now = clock.instant();
    if (startInstant.isBefore(now)) {
      return null;
    }
    return startInstant;
  }

  private ZoneId resolveZone() {
    if (timezone != null) {
      return timezone;
    }
    return ZoneId.systemDefault();
  }
}
