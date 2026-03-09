package net.brightroom.endpointgate.reactive.core.evaluation;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveScheduleProvider;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

/** Reactive evaluation step that checks whether the gate schedule is currently active. */
public class ReactiveScheduleEvaluationStep implements ReactiveEvaluationStep {

  private final ReactiveScheduleProvider scheduleProvider;
  private final Clock clock;

  /**
   * Creates a new {@code ReactiveScheduleEvaluationStep}.
   *
   * @param scheduleProvider the provider used to look up the schedule per gate
   * @param clock the clock used to obtain the current time for schedule evaluation
   */
  public ReactiveScheduleEvaluationStep(ReactiveScheduleProvider scheduleProvider, Clock clock) {
    this.scheduleProvider = scheduleProvider;
    this.clock = clock;
  }

  @Override
  public Mono<AccessDecision> evaluate(EvaluationContext context) {
    return scheduleProvider
        .getSchedule(context.gateId())
        .map(
            schedule -> {
              if (schedule.isActive(clock.instant())) {
                return AccessDecision.allowed();
              }
              return AccessDecision.denied(
                  context.gateId(), DeniedReason.SCHEDULE_INACTIVE, toRetryAfterInstant(schedule));
            })
        .defaultIfEmpty(AccessDecision.allowed());
  }

  private @Nullable Instant toRetryAfterInstant(Schedule schedule) {
    if (schedule.start() == null) {
      return null;
    }
    ZoneId zone;
    if (schedule.timezone() != null) {
      zone = schedule.timezone();
    } else {
      zone = ZoneId.systemDefault();
    }
    return schedule.start().atZone(zone).toInstant();
  }
}
