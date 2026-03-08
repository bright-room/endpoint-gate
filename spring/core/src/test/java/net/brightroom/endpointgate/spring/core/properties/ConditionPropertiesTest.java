package net.brightroom.endpointgate.spring.core.properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConditionPropertiesTest {

  @Test
  void failOnError_defaultsToTrue() {
    var props = new ConditionProperties();

    assertTrue(props.failOnError());
  }

  @Test
  void setFailOnError_updatesValue() {
    var props = new ConditionProperties();

    props.setFailOnError(false);

    assertFalse(props.failOnError());
  }
}
