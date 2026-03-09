package net.brightroom.endpointgate.spring.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class GlobalSchedulePropertiesTest {

  @Test
  void defaultTimezone_returnsNull_byDefault() {
    GlobalScheduleProperties properties = new GlobalScheduleProperties();

    assertNull(properties.defaultTimezone());
  }

  @Test
  void defaultTimezone_returnsConfiguredValue_afterSetDefaultTimezone() {
    GlobalScheduleProperties properties = new GlobalScheduleProperties();
    ZoneId tokyo = ZoneId.of("Asia/Tokyo");

    properties.setDefaultTimezone(tokyo);

    assertEquals(tokyo, properties.defaultTimezone());
  }
}
