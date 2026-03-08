package net.brightroom.example.customrolloutstrategy;

import java.util.Map;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.rollout.RolloutStrategy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("region")
public class RegionBasedRolloutStrategy implements RolloutStrategy {

  private static final Map<String, Integer> REGION_WEIGHT =
      Map.of(
          "us-east", 0,
          "eu-west", 50,
          "ap-northeast", 80);

  @Override
  public boolean isInRollout(String gateId, EndpointGateContext context, int percentage) {
    String region = context.userIdentifier();
    int weight = REGION_WEIGHT.getOrDefault(region, 100);
    return weight < percentage;
  }
}
