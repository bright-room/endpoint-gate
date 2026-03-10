package net.brightroom.example.schedule;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class CampaignController {

  @EndpointGate("campaign")
  @GetMapping("/api/campaign/deals")
  public Mono<String> campaignDeals() {
    return Mono.just("Special campaign deals available");
  }
}
