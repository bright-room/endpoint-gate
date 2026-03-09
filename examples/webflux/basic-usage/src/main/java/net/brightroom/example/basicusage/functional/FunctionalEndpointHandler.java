package net.brightroom.example.basicusage.functional;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class FunctionalEndpointHandler {

  public Mono<ServerResponse> enabledFeature(ServerRequest request) {
    return ServerResponse.ok().bodyValue("Enabled feature via functional endpoint");
  }

  public Mono<ServerResponse> disabledFeature(ServerRequest request) {
    return ServerResponse.ok().bodyValue("Disabled feature via functional endpoint");
  }

  public Mono<ServerResponse> noFilter(ServerRequest request) {
    return ServerResponse.ok().bodyValue("No filter applied — always accessible");
  }
}
