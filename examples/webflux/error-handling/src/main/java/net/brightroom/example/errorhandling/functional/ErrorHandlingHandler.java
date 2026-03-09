package net.brightroom.example.errorhandling.functional;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ErrorHandlingHandler {

  public Mono<ServerResponse> premiumFeature(ServerRequest request) {
    return ServerResponse.ok().bodyValue("Premium feature via functional endpoint");
  }
}
