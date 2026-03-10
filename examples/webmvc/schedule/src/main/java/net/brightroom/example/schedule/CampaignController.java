package net.brightroom.example.schedule;

import net.brightroom.endpointgate.core.annotation.EndpointGate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CampaignController {

  @EndpointGate("campaign")
  @GetMapping("/api/campaign/deals")
  public String campaignDeals() {
    return "Special campaign deals available";
  }
}
