package net.brightroom.endpointgate.spring.core.autoconfigure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import net.brightroom.endpointgate.core.properties.ResponseProperties;
import net.brightroom.endpointgate.core.properties.ResponseType;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {EndpointGateAutoConfiguration.class})
@ActiveProfiles("empty")
class EndpointGateAutoConfigurationEmptyPropertiesTest {

  EndpointGateProperties endpointGateProperties;

  @Test
  void shouldBeEmptyWhenPropertiesAreNotProvided() {
    Map<String, Boolean> gateIds = endpointGateProperties.gateIds();
    assertTrue(gateIds.isEmpty());

    assertFalse(endpointGateProperties.defaultEnabled());

    ResponseProperties response = endpointGateProperties.response();
    assertEquals(ResponseType.JSON, response.type());
  }

  @Autowired
  EndpointGateAutoConfigurationEmptyPropertiesTest(EndpointGateProperties endpointGateProperties) {
    this.endpointGateProperties = endpointGateProperties;
  }
}
