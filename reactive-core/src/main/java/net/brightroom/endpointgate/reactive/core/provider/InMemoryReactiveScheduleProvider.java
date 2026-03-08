package net.brightroom.endpointgate.reactive.core.provider;

import java.util.Map;
import net.brightroom.endpointgate.core.provider.Schedule;
import reactor.core.publisher.Mono;

/**
 * An implementation of {@link ReactiveScheduleProvider} that stores schedule configurations in
 * memory using a {@link Map}.
 *
 * <p>This class provides a simple, immutable in-memory mechanism to resolve gate schedules
 * reactively. When a gate has no configured schedule, an empty {@link Mono} is returned.
 */
public class InMemoryReactiveScheduleProvider implements ReactiveScheduleProvider {

  private final Map<String, Schedule> schedules;

  /**
   * {@inheritDoc}
   *
   * <p>Returns an empty {@link Mono} for gates not present in the schedule map.
   */
  @Override
  public Mono<Schedule> getSchedule(String gateId) {
    return Mono.justOrEmpty(schedules.get(gateId));
  }

  /**
   * Constructs an instance with the provided schedule configurations.
   *
   * @param schedules a map containing gate identifiers as keys and their schedule configurations as
   *     values; copied defensively on construction
   */
  public InMemoryReactiveScheduleProvider(Map<String, Schedule> schedules) {
    this.schedules = Map.copyOf(schedules);
  }
}
