package net.brightroom.endpointgate.reactive.core.provider;

import java.util.Map;
import net.brightroom.endpointgate.core.provider.Schedule;
import reactor.core.publisher.Mono;

/**
 * A reactive extension of {@link ReactiveScheduleProvider} that supports dynamic mutation of
 * schedules at runtime.
 *
 * <p>Implementations must be thread-safe, as schedules may be read and updated concurrently.
 *
 * <p>This interface serves as an SPI for the actuator endpoint to update schedules at runtime
 * without restarting the application.
 */
public interface MutableReactiveScheduleProvider extends ReactiveScheduleProvider {

  /**
   * Returns a snapshot of all currently configured schedules.
   *
   * <p>The returned map must be an immutable copy; mutations to the returned map must not affect
   * the provider's internal state.
   *
   * @return a {@link Mono} emitting an immutable map of gate identifiers to their schedules
   */
  Mono<Map<String, Schedule>> getSchedules();

  /**
   * Updates the schedule for the specified gate.
   *
   * <p>If the gate does not have a configured schedule, it is created.
   *
   * @param gateId the identifier of the gate to update
   * @param schedule the new schedule
   * @return a {@link Mono} that completes when the update is applied
   */
  Mono<Void> setSchedule(String gateId, Schedule schedule);

  /**
   * Removes the schedule for the specified gate.
   *
   * <p>If the gate does not have a configured schedule, this method is a no-op and emits {@code
   * false}.
   *
   * @param gateId the identifier of the gate whose schedule to remove
   * @return a {@link Mono} emitting {@code true} if the schedule existed and was removed, {@code
   *     false} if it did not exist
   */
  Mono<Boolean> removeSchedule(String gateId);
}
