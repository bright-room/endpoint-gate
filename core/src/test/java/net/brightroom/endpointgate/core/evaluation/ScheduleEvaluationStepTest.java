package net.brightroom.endpointgate.core.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.core.provider.ScheduleProvider;
import org.junit.jupiter.api.Test;

class ScheduleEvaluationStepTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();
  private static final EvaluationContext CTX =
      new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> null);

  // Fixed clock at 2025-06-15T12:00:00Z
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2025-06-15T12:00:00Z"), ZoneId.of("UTC"));

  private final ScheduleProvider scheduleProvider = mock(ScheduleProvider.class);
  private final ScheduleEvaluationStep step = new ScheduleEvaluationStep(scheduleProvider, CLOCK);

  @Test
  void evaluate_returnsEmpty_whenNoScheduleConfigured() {
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.empty());
    assertThat(step.evaluate(CTX)).isEmpty();
  }

  @Test
  void evaluate_returnsEmpty_whenScheduleIsActive() {
    // start in past, no end → active
    Schedule active = new Schedule(LocalDateTime.of(2025, 1, 1, 0, 0), null, ZoneId.of("UTC"));
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.of(active));
    assertThat(step.evaluate(CTX)).isEmpty();
  }

  @Test
  void evaluate_returnsDenied_whenScheduleIsInactive_endInPast() {
    // end in past, no start → inactive; retryAfter should be null
    Schedule inactive = new Schedule(null, LocalDateTime.of(2025, 1, 1, 0, 0), ZoneId.of("UTC"));
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.of(inactive));

    Optional<AccessDecision> result = step.evaluate(CTX);
    assertThat(result).isPresent();
    AccessDecision.Denied denied = (AccessDecision.Denied) result.get();
    assertThat(denied.gateId()).isEqualTo("my-gate");
    assertThat(denied.reason()).isEqualTo(DeniedReason.SCHEDULE_INACTIVE);
    assertThat(denied.retryAfter()).isNull();
  }

  @Test
  void evaluate_returnsDenied_whenScheduleIsInactive_startInFuture() {
    // start in future → inactive; retryAfter should be the start instant
    LocalDateTime futureStart = LocalDateTime.of(2025, 12, 1, 0, 0);
    Schedule inactive = new Schedule(futureStart, null, ZoneId.of("UTC"));
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.of(inactive));

    Optional<AccessDecision> result = step.evaluate(CTX);
    assertThat(result).isPresent();
    AccessDecision.Denied denied = (AccessDecision.Denied) result.get();
    assertThat(denied.reason()).isEqualTo(DeniedReason.SCHEDULE_INACTIVE);
    assertThat(denied.retryAfter()).isEqualTo(futureStart.atZone(ZoneId.of("UTC")).toInstant());
  }

  @Test
  void evaluate_returnsDenied_withTimezoneAwareRetryAfter_whenTimezoneConfigured() {
    // start in future with Tokyo timezone
    LocalDateTime futureStart = LocalDateTime.of(2025, 12, 1, 9, 0);
    ZoneId tokyo = ZoneId.of("Asia/Tokyo");
    Schedule inactive = new Schedule(futureStart, null, tokyo);
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.of(inactive));

    Optional<AccessDecision> result = step.evaluate(CTX);
    assertThat(result).isPresent();
    AccessDecision.Denied denied = (AccessDecision.Denied) result.get();
    assertThat(denied.retryAfter()).isEqualTo(futureStart.atZone(tokyo).toInstant());
  }
}
