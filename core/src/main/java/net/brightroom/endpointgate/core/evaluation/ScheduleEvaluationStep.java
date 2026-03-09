package net.brightroom.endpointgate.core.evaluation;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.core.provider.ScheduleProvider;
import org.jspecify.annotations.Nullable;

/** Evaluation step that checks whether the gate schedule is currently active. */
public class ScheduleEvaluationStep implements EvaluationStep {

  private final ScheduleProvider scheduleProvider;
  private final Clock clock;

  /**
   * Creates a new {@code ScheduleEvaluationStep}.
   *
   * @param scheduleProvider the provider used to look up the schedule per gate
   * @param clock the clock used to obtain the current time for schedule evaluation
   */
  public ScheduleEvaluationStep(ScheduleProvider scheduleProvider, Clock clock) {
    this.scheduleProvider = scheduleProvider;
    this.clock = clock;
  }

  @Override
  public Optional<AccessDecision> evaluate(EvaluationContext context) {
    return scheduleProvider
        .getSchedule(context.gateId())
        .filter(schedule -> !schedule.isActive(clock.instant()))
        .map(
            schedule ->
                AccessDecision.denied(
                    context.gateId(),
                    DeniedReason.SCHEDULE_INACTIVE,
                    toRetryAfterInstant(schedule)));
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
