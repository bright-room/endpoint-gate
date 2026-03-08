package net.brightroom.endpointgate.spring.core.resolution;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class ProblemDetailBuilderTest {

  @Test
  void build_setsForbiddenStatus() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    ProblemDetail result = ProblemDetailBuilder.build("/api/resource", e);
    assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void build_setsTypeUri() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    ProblemDetail result = ProblemDetailBuilder.build("/api/resource", e);
    assertThat(result.getType())
        .isEqualTo(URI.create("https://github.com/bright-room/endpoint-gate#response-types"));
  }

  @Test
  void build_setsTitle() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    ProblemDetail result = ProblemDetailBuilder.build("/api/resource", e);
    assertThat(result.getTitle()).isEqualTo("Endpoint gate access denied");
  }

  @Test
  void build_setsDetailFromExceptionMessage() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    ProblemDetail result = ProblemDetailBuilder.build("/api/resource", e);
    assertThat(result.getDetail()).isEqualTo(e.getMessage());
  }

  @Test
  void build_setsInstanceFromRequestPath() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    ProblemDetail result = ProblemDetailBuilder.build("/api/resource", e);
    assertThat(result.getInstance()).isEqualTo(URI.create("/api/resource"));
  }
}
