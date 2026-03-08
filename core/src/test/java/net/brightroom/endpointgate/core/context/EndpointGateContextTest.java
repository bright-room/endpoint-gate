package net.brightroom.endpointgate.core.context;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EndpointGateContextTest {

  @Test
  void constructor_throwsNullPointerException_whenUserIdentifierIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new EndpointGateContext(null))
        .withMessageContaining("userIdentifier");
  }

  @Test
  void constructor_throwsIllegalArgumentException_whenUserIdentifierIsBlank() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new EndpointGateContext("  "))
        .withMessageContaining("userIdentifier");
  }

  @Test
  void constructor_throwsIllegalArgumentException_whenUserIdentifierIsEmpty() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new EndpointGateContext(""))
        .withMessageContaining("userIdentifier");
  }

  @Test
  void constructor_success_whenUserIdentifierIsValid() {
    EndpointGateContext context = new EndpointGateContext("user-123");
    assertEquals("user-123", context.userIdentifier());
  }
}
