package net.brightroom.endpointgate.core.validation;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class GateIdValidatorTest {

  @Test
  void validateGateIds_succeeds_whenSingleValidGateId() {
    assertDoesNotThrow(() -> GateIdValidator.validateGateIds(new String[] {"my-gate"}));
  }

  @Test
  void validateGateIds_succeeds_whenMultipleValidGateIds() {
    assertDoesNotThrow(
        () -> GateIdValidator.validateGateIds(new String[] {"gate-a", "gate-b", "gate-c"}));
  }

  @Test
  void validateGateIds_throwsIllegalArgumentException_whenNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> GateIdValidator.validateGateIds(null))
        .withMessageContaining("null or empty");
  }

  @Test
  void validateGateIds_throwsIllegalArgumentException_whenEmptyArray() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> GateIdValidator.validateGateIds(new String[] {}))
        .withMessageContaining("null or empty");
  }

  @Test
  void validateGateIds_throwsIllegalArgumentException_whenElementIsNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> GateIdValidator.validateGateIds(new String[] {"gate-a", null}))
        .withMessageContaining("null or blank");
  }

  @Test
  void validateGateIds_throwsIllegalArgumentException_whenElementIsEmpty() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> GateIdValidator.validateGateIds(new String[] {""}))
        .withMessageContaining("null or blank");
  }

  @Test
  void validateGateIds_throwsIllegalArgumentException_whenElementIsBlank() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> GateIdValidator.validateGateIds(new String[] {"  "}))
        .withMessageContaining("null or blank");
  }
}
