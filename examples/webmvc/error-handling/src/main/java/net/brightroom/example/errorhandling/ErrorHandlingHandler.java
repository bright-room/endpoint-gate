package net.brightroom.example.errorhandling;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/** Handler for functional endpoint requests. */
@Component
@Profile("functional-error")
public class ErrorHandlingHandler {

  /**
   * Handles the gated endpoint request.
   *
   * @param request the server request
   * @return the server response
   */
  public ServerResponse gated(ServerRequest request) {
    return ServerResponse.ok().body("Functional endpoint data");
  }
}
