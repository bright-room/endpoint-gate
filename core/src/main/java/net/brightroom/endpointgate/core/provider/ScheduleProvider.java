package net.brightroom.endpointgate.core.provider;

import java.util.Optional;

/**
 * SPI for resolving the schedule configuration for a given endpoint gate.
 *
 * <p>Implementations provide the configured schedule for each gate. When a gate has no configured
 * schedule, {@link Optional#empty()} is returned and the caller treats the schedule as always
 * active.
 */
public interface ScheduleProvider {

  /**
   * Returns the configured schedule for the specified gate.
   *
   * @param gateId the identifier of the gate
   * @return an {@link Optional} containing the {@link Schedule}, or {@link Optional#empty()} if no
   *     schedule is configured for this gate
   */
  Optional<Schedule> getSchedule(String gateId);
}
