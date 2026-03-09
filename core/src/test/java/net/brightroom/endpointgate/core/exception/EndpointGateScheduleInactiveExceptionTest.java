package net.brightroom.endpointgate.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class EndpointGateScheduleInactiveExceptionTest {

  @Test
  void retryAfter_returnsProvidedInstant() {
    Instant retryAfter = Instant.parse("2099-01-01T00:00:00Z");
    var e = new EndpointGateScheduleInactiveException("my-gate", retryAfter);
    assertThat(e.retryAfter()).isEqualTo(retryAfter);
  }

  @Test
  void retryAfter_returnsNull_whenNotProvided() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    assertThat(e.retryAfter()).isNull();
  }

  @Test
  void getMessage_containsGateId() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    assertThat(e.getMessage()).contains("my-gate");
  }

  @Test
  void getMessage_containsRetryAfterInstant_whenProvided() {
    Instant retryAfter = Instant.parse("2099-01-01T00:00:00Z");
    var e = new EndpointGateScheduleInactiveException("my-gate", retryAfter);
    assertThat(e.getMessage()).contains(retryAfter.toString());
  }

  @Test
  void getMessage_doesNotContainUntil_whenRetryAfterIsNull() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    assertThat(e.getMessage()).doesNotContain("until");
  }

  @Test
  void isInstanceOfEndpointGateAccessDeniedException() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    assertThat(e).isInstanceOf(EndpointGateAccessDeniedException.class);
  }

  @Test
  void gateId_returnsProvidedGateId() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    assertThat(e.gateId()).isEqualTo("my-gate");
  }
}
