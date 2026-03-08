package net.brightroom.example.customrolloutstrategy;

import java.time.LocalTime;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.rollout.DefaultRolloutStrategy;
import net.brightroom.endpointgate.core.rollout.RolloutStrategy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("time-based")
public class TimeBasedRolloutStrategy implements RolloutStrategy {

  @Override
  public boolean isInRollout(String gateId, EndpointGateContext context, int percentage) {
    int hour = LocalTime.now().getHour();
    if (hour < 9 || hour >= 17) {
      return false;
    }
    return new DefaultRolloutStrategy().isInRollout(gateId, context, percentage);
  }
}
