package net.brightroom.endpointgate.core.provider;

import java.util.Map;

/**
 * An extension of {@link ScheduleProvider} that supports dynamic mutation of schedules at runtime.
 *
 * <p>Implementations must be thread-safe, as schedules may be read and updated concurrently.
 */
public interface MutableScheduleProvider extends ScheduleProvider {

  /**
   * Returns a snapshot of all currently configured schedules.
   *
   * <p>The returned map must be an immutable copy; mutations to the returned map must not affect
   * the provider's internal state.
   *
   * @return an immutable map of gate identifiers to their schedules
   */
  Map<String, Schedule> getSchedules();

  /**
   * Updates the schedule for the specified gate.
   *
   * <p>If the gate does not have a configured schedule, it is created.
   *
   * @param gateId the identifier of the gate to update
   * @param schedule the new schedule
   */
  void setSchedule(String gateId, Schedule schedule);

  /**
   * Removes the schedule for the specified gate.
   *
   * <p>If the gate does not have a configured schedule, this method is a no-op.
   *
   * @param gateId the identifier of the gate whose schedule to remove
   */
  void removeSchedule(String gateId);
}
