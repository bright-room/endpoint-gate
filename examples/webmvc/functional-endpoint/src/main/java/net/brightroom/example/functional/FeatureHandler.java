package net.brightroom.example.functional;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class FeatureHandler {

  public ServerResponse stable(ServerRequest request) {
    return ServerResponse.ok().body("stable-api: this endpoint is generally available.");
  }

  public ServerResponse beta(ServerRequest request) {
    return ServerResponse.ok().body("beta-api: you are in the rollout group!");
  }

  public ServerResponse experimental(ServerRequest request) {
    return ServerResponse.ok().body("experimental-api: this endpoint is under development.");
  }
}
