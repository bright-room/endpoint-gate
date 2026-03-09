package net.brightroom.endpointgate.core.provider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe, in-memory implementation of {@link MutableScheduleProvider}.
 *
 * <p>Schedules are stored in a {@link ConcurrentHashMap}, allowing concurrent reads and writes
 * without external synchronization.
 */
public class MutableInMemoryScheduleProvider implements MutableScheduleProvider {

  private final ConcurrentHashMap<String, Schedule> schedules;

  /** {@inheritDoc} */
  @Override
  public Optional<Schedule> getSchedule(String gateId) {
    return Optional.ofNullable(schedules.get(gateId));
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, Schedule> getSchedules() {
    return Map.copyOf(schedules);
  }

  /** {@inheritDoc} */
  @Override
  public void setSchedule(String gateId, Schedule schedule) {
    schedules.put(gateId, schedule);
  }

  /** {@inheritDoc} */
  @Override
  public void removeSchedule(String gateId) {
    schedules.remove(gateId);
  }

  /**
   * Constructs a {@code MutableInMemoryScheduleProvider} with the given initial schedules.
   *
   * @param schedules the initial schedule map; copied defensively on construction
   */
  public MutableInMemoryScheduleProvider(Map<String, Schedule> schedules) {
    this.schedules = new ConcurrentHashMap<>(schedules);
  }
}
