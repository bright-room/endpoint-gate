package net.brightroom.example.functional;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class FeatureHandler {

  public Mono<ServerResponse> stable(ServerRequest request) {
    return ServerResponse.ok().bodyValue("stable-api: this endpoint is generally available.");
  }

  public Mono<ServerResponse> beta(ServerRequest request) {
    return ServerResponse.ok().bodyValue("beta-api: you are in the rollout group!");
  }

  public Mono<ServerResponse> experimental(ServerRequest request) {
    return ServerResponse.ok().bodyValue("experimental-api: this endpoint is under development.");
  }

  public Mono<ServerResponse> internal(ServerRequest request) {
    return ServerResponse.ok().bodyValue("internal-api: this endpoint is for internal use only.");
  }

  public Mono<ServerResponse> preview(ServerRequest request) {
    return ServerResponse.ok().bodyValue("preview-feature: you are in the preview group!");
  }

  public Mono<ServerResponse> restricted(ServerRequest request) {
    return ServerResponse.ok().bodyValue("restricted: accessible only when all gates are enabled.");
  }
}
