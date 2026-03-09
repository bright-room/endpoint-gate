package net.brightroom.endpointgate.core.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ScheduleTest {

  // Fixed reference instant: 2026-06-15T12:00:00 UTC
  private final Instant now = Instant.parse("2026-06-15T12:00:00Z");
  // Fixed clock at the same reference instant
  private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("UTC"));

  // --- start only ---

  @Test
  void isActive_returnsFalse_whenNowIsBeforeStart() {
    Schedule schedule = new Schedule(LocalDateTime.of(2026, 6, 15, 13, 0), null, ZoneId.of("UTC"));

    assertFalse(schedule.isActive(now));
  }

  @Test
  void isActive_returnsTrue_whenNowIsAfterStart() {
    Schedule schedule = new Schedule(LocalDateTime.of(2026, 6, 15, 11, 0), null, ZoneId.of("UTC"));

    assertTrue(schedule.isActive(now));
  }

  // --- end only ---

  @Test
  void isActive_returnsTrue_whenNowIsBeforeEnd() {
    Schedule schedule = new Schedule(null, LocalDateTime.of(2026, 6, 15, 13, 0), ZoneId.of("UTC"));

    assertTrue(schedule.isActive(now));
  }

  @Test
  void isActive_returnsFalse_whenNowIsAfterEnd() {
    Schedule schedule = new Schedule(null, LocalDateTime.of(2026, 6, 15, 11, 0), ZoneId.of("UTC"));

    assertFalse(schedule.isActive(now));
  }

  // --- start + end ---

  @Test
  void isActive_returnsTrue_whenNowIsWithinRange() {
    Schedule schedule =
        new Schedule(
            LocalDateTime.of(2026, 6, 15, 11, 0),
            LocalDateTime.of(2026, 6, 15, 13, 0),
            ZoneId.of("UTC"));

    assertTrue(schedule.isActive(now));
  }

  @Test
  void isActive_returnsFalse_whenNowIsBeforeRange() {
    Schedule schedule =
        new Schedule(
            LocalDateTime.of(2026, 6, 15, 13, 0),
            LocalDateTime.of(2026, 6, 15, 15, 0),
            ZoneId.of("UTC"));

    assertFalse(schedule.isActive(now));
  }

  @Test
  void isActive_returnsFalse_whenNowIsAfterRange() {
    Schedule schedule =
        new Schedule(
            LocalDateTime.of(2026, 6, 15, 9, 0),
            LocalDateTime.of(2026, 6, 15, 11, 0),
            ZoneId.of("UTC"));

    assertFalse(schedule.isActive(now));
  }

  // --- neither start nor end ---

  @Test
  void isActive_returnsTrue_whenBothStartAndEndAreNull() {
    Schedule schedule = new Schedule(null, null, null);

    assertTrue(schedule.isActive(now));
  }

  // --- timezone ---

  @Test
  void isActive_appliesTimezone_whenTimezoneIsConfigured() {
    Schedule schedule =
        new Schedule(LocalDateTime.of(2026, 6, 15, 20, 0), null, ZoneId.of("Asia/Tokyo"));

    assertTrue(schedule.isActive(now));
  }

  @Test
  void isActive_returnsFalse_usingTimezone_whenBeforeStart() {
    Schedule schedule =
        new Schedule(LocalDateTime.of(2026, 6, 15, 22, 0), null, ZoneId.of("Asia/Tokyo"));

    assertFalse(schedule.isActive(now));
  }

  @Test
  void isActive_usesSystemDefaultTimezone_whenTimezoneIsNull() {
    Schedule schedule = new Schedule(null, null, null);

    assertTrue(schedule.isActive(now));
  }

  // --- boundary values ---

  @Test
  void isActive_returnsTrue_whenNowEqualsStart() {
    Schedule schedule = new Schedule(LocalDateTime.of(2026, 6, 15, 12, 0), null, ZoneId.of("UTC"));

    assertTrue(schedule.isActive(now));
  }

  @Test
  void isActive_returnsTrue_whenNowEqualsEnd() {
    Schedule schedule = new Schedule(null, LocalDateTime.of(2026, 6, 15, 12, 0), ZoneId.of("UTC"));

    assertTrue(schedule.isActive(now));
  }

  // --- constructor validation ---

  @Test
  void constructor_throwsIllegalArgumentException_whenStartIsAfterEnd() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new Schedule(
                    LocalDateTime.of(2026, 6, 15, 13, 0),
                    LocalDateTime.of(2026, 6, 15, 12, 0),
                    ZoneId.of("UTC")))
        .withMessageContaining("Schedule start must not be after end");
  }

  @Test
  void constructor_doesNotThrow_whenStartEqualsEnd() {
    assertThatNoException()
        .isThrownBy(
            () ->
                new Schedule(
                    LocalDateTime.of(2026, 6, 15, 12, 0),
                    LocalDateTime.of(2026, 6, 15, 12, 0),
                    ZoneId.of("UTC")));
  }

  @Test
  void constructor_doesNotThrow_whenStartIsNull() {
    assertThatNoException()
        .isThrownBy(() -> new Schedule(null, LocalDateTime.of(2026, 6, 15, 12, 0), null));
  }

  @Test
  void constructor_doesNotThrow_whenEndIsNull() {
    assertThatNoException()
        .isThrownBy(() -> new Schedule(LocalDateTime.of(2026, 6, 15, 12, 0), null, null));
  }

  // --- retryAfterInstant ---

  @Test
  void retryAfterInstant_returnsNull_whenStartIsNull() {
    Schedule schedule = new Schedule(null, LocalDateTime.of(2026, 6, 15, 11, 0), ZoneId.of("UTC"));

    assertThat(schedule.retryAfterInstant(clock)).isNull();
  }

  @Test
  void retryAfterInstant_returnsNull_whenStartIsInThePast() {
    // start well before the clock instant
    Schedule schedule = new Schedule(LocalDateTime.of(2026, 1, 1, 0, 0), null, ZoneId.of("UTC"));

    assertThat(schedule.retryAfterInstant(clock)).isNull();
  }

  @Test
  void retryAfterInstant_returnsStartInstant_whenStartIsInTheFuture() {
    LocalDateTime futureStart = LocalDateTime.of(2026, 12, 1, 0, 0);
    Schedule schedule = new Schedule(futureStart, null, ZoneId.of("UTC"));
    Instant expected = futureStart.atZone(ZoneId.of("UTC")).toInstant();

    assertThat(schedule.retryAfterInstant(clock)).isEqualTo(expected);
  }

  @Test
  void retryAfterInstant_appliesTimezone_whenTimezoneIsConfigured() {
    LocalDateTime futureStart = LocalDateTime.of(2026, 12, 1, 9, 0);
    ZoneId tokyo = ZoneId.of("Asia/Tokyo");
    Schedule schedule = new Schedule(futureStart, null, tokyo);
    Instant expected = futureStart.atZone(tokyo).toInstant();

    assertThat(schedule.retryAfterInstant(clock)).isEqualTo(expected);
  }

  @Test
  void retryAfterInstant_usesSystemDefaultTimezone_whenTimezoneIsNull() {
    // start in the future regardless of timezone
    LocalDateTime futureStart = LocalDateTime.of(2026, 12, 1, 0, 0);
    Schedule schedule = new Schedule(futureStart, null, null);

    assertThat(schedule.retryAfterInstant(clock)).isNotNull();
  }

  // --- explicit UTC offset ---

  @Test
  void isActive_worksWithExplicitUtcOffset() {
    Schedule schedule =
        new Schedule(
            LocalDateTime.of(2026, 6, 15, 8, 0), null, ZoneId.from(ZoneOffset.ofHours(-3)));

    assertTrue(schedule.isActive(now));
  }
}
