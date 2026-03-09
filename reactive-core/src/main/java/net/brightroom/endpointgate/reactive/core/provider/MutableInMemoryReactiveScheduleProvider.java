package net.brightroom.endpointgate.reactive.core.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.brightroom.endpointgate.core.provider.Schedule;
import reactor.core.publisher.Mono;

/**
 * A thread-safe, in-memory implementation of {@link MutableReactiveScheduleProvider}.
 *
 * <p>Schedules are stored in a {@link ConcurrentHashMap}, allowing concurrent reads and writes
 * without external synchronization.
 */
public class MutableInMemoryReactiveScheduleProvider implements MutableReactiveScheduleProvider {

  private final ConcurrentHashMap<String, Schedule> schedules;

  /**
   * {@inheritDoc}
   *
   * <p>Returns an empty {@link Mono} for gates not present in the schedule map.
   */
  @Override
  public Mono<Schedule> getSchedule(String gateId) {
    return Mono.justOrEmpty(schedules.get(gateId));
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Map<String, Schedule>> getSchedules() {
    return Mono.just(Map.copyOf(schedules));
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Void> setSchedule(String gateId, Schedule schedule) {
    return Mono.<Void>fromRunnable(() -> schedules.put(gateId, schedule));
  }

  /** {@inheritDoc} */
  @Override
  public Mono<Boolean> removeSchedule(String gateId) {
    return Mono.fromCallable(() -> schedules.remove(gateId) != null);
  }

  /**
   * Constructs a {@code MutableInMemoryReactiveScheduleProvider} with the given initial schedules.
   *
   * @param schedules the initial schedule map; copied defensively on construction
   */
  public MutableInMemoryReactiveScheduleProvider(Map<String, Schedule> schedules) {
    this.schedules = new ConcurrentHashMap<>(schedules);
  }
}
