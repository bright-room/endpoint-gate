package net.brightroom.example.customrolloutstrategy;

import java.time.LocalTime;
import java.time.ZoneId;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.reactive.core.rollout.DefaultReactiveRolloutStrategy;
import net.brightroom.endpointgate.reactive.core.rollout.ReactiveRolloutStrategy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("time-based")
public class TimeBasedReactiveRolloutStrategy implements ReactiveRolloutStrategy {

  private static final LocalTime BUSINESS_START = LocalTime.of(9, 0);
  private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);
  private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");

  private final DefaultReactiveRolloutStrategy defaultStrategy =
      new DefaultReactiveRolloutStrategy();

  @Override
  public Mono<Boolean> isInRollout(String gateId, EndpointGateContext context, int percentage) {
    LocalTime now = LocalTime.now(ZONE);
    if (now.isBefore(BUSINESS_START)) {
      return Mono.just(false);
    }
    if (!now.isBefore(BUSINESS_END)) {
      return Mono.just(false);
    }
    return defaultStrategy.isInRollout(gateId, context, percentage);
  }
}
