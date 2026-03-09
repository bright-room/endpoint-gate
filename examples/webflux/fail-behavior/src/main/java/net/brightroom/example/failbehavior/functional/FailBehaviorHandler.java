package net.brightroom.example.failbehavior.functional;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class FailBehaviorHandler {

  public Mono<ServerResponse> knownFeature(ServerRequest request) {
    return ServerResponse.ok().bodyValue("Known feature via functional endpoint");
  }

  public Mono<ServerResponse> undefinedFeature(ServerRequest request) {
    return ServerResponse.ok().bodyValue("Undefined feature via functional endpoint");
  }

  public Mono<ServerResponse> knownDisabledFeature(ServerRequest request) {
    return ServerResponse.ok()
        .bodyValue("This should not be reached (explicitly disabled, functional)");
  }
}
