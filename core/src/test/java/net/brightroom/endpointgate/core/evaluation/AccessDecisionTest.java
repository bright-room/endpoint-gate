package net.brightroom.endpointgate.core.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import org.junit.jupiter.api.Test;

class AccessDecisionTest {

  @Test
  void allowed_returnsAllowedInstance() {
    AccessDecision decision = AccessDecision.allowed();
    assertThat(decision).isInstanceOf(AccessDecision.Allowed.class);
  }

  @Test
  void denied_returnsDeniedInstanceWithGateIdAndReason() {
    AccessDecision decision = AccessDecision.denied("my-gate", DeniedReason.DISABLED);
    assertThat(decision).isInstanceOf(AccessDecision.Denied.class);
    AccessDecision.Denied denied = (AccessDecision.Denied) decision;
    assertThat(denied.gateId()).isEqualTo("my-gate");
    assertThat(denied.reason()).isEqualTo(DeniedReason.DISABLED);
    assertThat(denied.retryAfter()).isNull();
  }

  @Test
  void denied_supportsAllReasons() {
    for (DeniedReason reason : DeniedReason.values()) {
      AccessDecision decision = AccessDecision.denied("gate", reason);
      assertThat(((AccessDecision.Denied) decision).reason()).isEqualTo(reason);
    }
  }

  @Test
  void denied_withRetryAfter_storesInstant() {
    Instant retryAfter = Instant.parse("2099-01-01T00:00:00Z");
    AccessDecision decision =
        AccessDecision.denied("my-gate", DeniedReason.SCHEDULE_INACTIVE, retryAfter);
    AccessDecision.Denied denied = (AccessDecision.Denied) decision;
    assertThat(denied.retryAfter()).isEqualTo(retryAfter);
  }

  @Test
  void toException_returnsScheduleInactiveException_whenReasonIsScheduleInactive() {
    Instant retryAfter = Instant.parse("2099-01-01T00:00:00Z");
    AccessDecision.Denied denied =
        (AccessDecision.Denied)
            AccessDecision.denied("my-gate", DeniedReason.SCHEDULE_INACTIVE, retryAfter);
    EndpointGateAccessDeniedException e = denied.toException();
    assertThat(e).isInstanceOf(EndpointGateScheduleInactiveException.class);
    EndpointGateScheduleInactiveException scheduleException =
        (EndpointGateScheduleInactiveException) e;
    assertThat(scheduleException.gateId()).isEqualTo("my-gate");
    assertThat(scheduleException.retryAfter()).isEqualTo(retryAfter);
  }

  @Test
  void toException_returnsAccessDeniedException_whenReasonIsDisabled() {
    AccessDecision.Denied denied =
        (AccessDecision.Denied) AccessDecision.denied("my-gate", DeniedReason.DISABLED);
    EndpointGateAccessDeniedException e = denied.toException();
    assertThat(e).isExactlyInstanceOf(EndpointGateAccessDeniedException.class);
    assertThat(e.gateId()).isEqualTo("my-gate");
  }

  @Test
  void toException_returnsAccessDeniedException_whenReasonIsConditionNotMet() {
    AccessDecision.Denied denied =
        (AccessDecision.Denied) AccessDecision.denied("my-gate", DeniedReason.CONDITION_NOT_MET);
    EndpointGateAccessDeniedException e = denied.toException();
    assertThat(e).isExactlyInstanceOf(EndpointGateAccessDeniedException.class);
  }

  @Test
  void toException_returnsAccessDeniedException_whenReasonIsRolloutExcluded() {
    AccessDecision.Denied denied =
        (AccessDecision.Denied) AccessDecision.denied("my-gate", DeniedReason.ROLLOUT_EXCLUDED);
    EndpointGateAccessDeniedException e = denied.toException();
    assertThat(e).isExactlyInstanceOf(EndpointGateAccessDeniedException.class);
  }
}
