package net.brightroom.endpointgate.reactive.core.provider;

import net.brightroom.endpointgate.core.provider.Schedule;
import reactor.core.publisher.Mono;

/**
 * Reactive SPI for resolving the schedule configuration for a given gate.
 *
 * <p>Implementations provide the configured schedule for each gate. When a gate has no configured
 * schedule, an empty {@link Mono} is returned and the caller treats the schedule as always active.
 *
 * <p>Implement this interface and register it as a Spring bean to override the default in-memory
 * provider. For example, to read schedules from a reactive data source.
 */
public interface ReactiveScheduleProvider {

  /**
   * Returns the configured schedule for the specified gate.
   *
   * @param gateId the identifier of the gate
   * @return a {@link Mono} emitting the {@link Schedule}, or an empty {@link Mono} if no schedule
   *     is configured for this gate
   */
  Mono<Schedule> getSchedule(String gateId);
}
