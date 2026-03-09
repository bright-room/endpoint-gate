package net.brightroom.endpointgate.reactive.core.evaluation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import net.brightroom.endpointgate.core.condition.ConditionVariables;
import net.brightroom.endpointgate.core.condition.ConditionVariablesBuilder;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveScheduleProvider;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveScheduleEvaluationStepTest {

  private static final ConditionVariables EMPTY_VARS = new ConditionVariablesBuilder().build();
  private static final EvaluationContext CTX =
      new EvaluationContext("my-gate", "", 100, EMPTY_VARS, () -> null);

  // Fixed clock at 2025-06-15T12:00:00Z
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2025-06-15T12:00:00Z"), ZoneId.of("UTC"));

  private final ReactiveScheduleProvider scheduleProvider = mock(ReactiveScheduleProvider.class);
  private final ReactiveScheduleEvaluationStep step =
      new ReactiveScheduleEvaluationStep(scheduleProvider, CLOCK);

  @Test
  void evaluate_returnsAllowed_whenNoScheduleConfigured() {
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Mono.empty());
    StepVerifier.create(step.evaluate(CTX))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
  }

  @Test
  void evaluate_returnsAllowed_whenScheduleIsActive() {
    // start in past, no end → active
    Schedule active = new Schedule(LocalDateTime.of(2025, 1, 1, 0, 0), null, ZoneId.of("UTC"));
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Mono.just(active));
    StepVerifier.create(step.evaluate(CTX))
        .expectNextMatches(d -> d instanceof AccessDecision.Allowed)
        .verifyComplete();
  }

  @Test
  void evaluate_returnsDenied_whenScheduleIsInactive_endInPast() {
    // end in past, no start → inactive; retryAfter should be null
    Schedule inactive = new Schedule(null, LocalDateTime.of(2025, 1, 1, 0, 0), ZoneId.of("UTC"));
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Mono.just(inactive));
    StepVerifier.create(step.evaluate(CTX))
        .expectNextMatches(
            d ->
                d instanceof AccessDecision.Denied denied
                    && denied.gateId().equals("my-gate")
                    && denied.reason() == DeniedReason.SCHEDULE_INACTIVE
                    && denied.retryAfter() == null)
        .verifyComplete();
  }

  @Test
  void evaluate_returnsDenied_whenScheduleIsInactive_startInFuture() {
    // start in future → inactive; retryAfter should be the start instant
    LocalDateTime futureStart = LocalDateTime.of(2025, 12, 1, 0, 0);
    Schedule inactive = new Schedule(futureStart, null, ZoneId.of("UTC"));
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Mono.just(inactive));
    Instant expectedRetryAfter = futureStart.atZone(ZoneId.of("UTC")).toInstant();
    StepVerifier.create(step.evaluate(CTX))
        .expectNextMatches(
            d ->
                d instanceof AccessDecision.Denied denied
                    && denied.reason() == DeniedReason.SCHEDULE_INACTIVE
                    && expectedRetryAfter.equals(denied.retryAfter()))
        .verifyComplete();
  }
}
