package net.brightroom.endpointgate.core.provider;

import java.util.Map;
import java.util.Optional;

/**
 * An implementation of {@link ScheduleProvider} that stores schedule configurations in memory using
 * a {@link Map}.
 */
public class InMemoryScheduleProvider implements ScheduleProvider {

  private final Map<String, Schedule> schedules;

  /** {@inheritDoc} */
  @Override
  public Optional<Schedule> getSchedule(String gateId) {
    return Optional.ofNullable(schedules.get(gateId));
  }

  /**
   * Constructs an instance with the provided schedule configurations.
   *
   * @param schedules a map containing gate identifiers as keys and their schedule configurations as
   *     values; copied defensively on construction
   */
  public InMemoryScheduleProvider(Map<String, Schedule> schedules) {
    this.schedules = Map.copyOf(schedules);
  }
}
