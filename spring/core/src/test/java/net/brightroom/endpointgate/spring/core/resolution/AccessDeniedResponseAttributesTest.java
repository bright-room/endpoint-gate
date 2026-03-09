package net.brightroom.endpointgate.spring.core.resolution;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AccessDeniedResponseAttributesTest {

  @Test
  void resolveStatus_returnsForbidden_whenAccessDeniedException() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    HttpStatus status = AccessDeniedResponseAttributes.resolveStatus(e);
    assertThat(status).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void resolveStatus_returnsServiceUnavailable_whenScheduleInactiveException() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    HttpStatus status = AccessDeniedResponseAttributes.resolveStatus(e);
    assertThat(status).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
  }

  @Test
  void formatRetryAfter_returnsNull_whenAccessDeniedException() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    String result = AccessDeniedResponseAttributes.formatRetryAfter(e);
    assertThat(result).isNull();
  }

  @Test
  void formatRetryAfter_returnsNull_whenScheduleInactiveWithNullRetryAfter() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    String result = AccessDeniedResponseAttributes.formatRetryAfter(e);
    assertThat(result).isNull();
  }

  @Test
  void formatRetryAfter_returnsRfc1123FormattedString_whenScheduleInactiveWithRetryAfter() {
    Instant retryAfter = Instant.parse("2099-01-01T00:00:00Z");
    var e = new EndpointGateScheduleInactiveException("my-gate", retryAfter);
    String result = AccessDeniedResponseAttributes.formatRetryAfter(e);
    assertThat(result).isEqualTo("Thu, 1 Jan 2099 00:00:00 GMT");
  }

  @Test
  void formatRetryAfter_matchesRfc1123Pattern() {
    Instant retryAfter = Instant.parse("2099-06-15T14:30:00Z");
    var e = new EndpointGateScheduleInactiveException("my-gate", retryAfter);
    String result = AccessDeniedResponseAttributes.formatRetryAfter(e);
    assertThat(result).matches("\\w{3}, \\d{1,2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} GMT");
  }
}
