package net.brightroom.endpointgate.spring.core.autoconfigure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import net.brightroom.endpointgate.core.properties.ResponseType;
import net.brightroom.endpointgate.spring.core.properties.EndpointGateProperties;
import net.brightroom.endpointgate.spring.core.properties.ResponseProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {EndpointGateAutoConfiguration.class})
@TestPropertySource(
    properties = {
      "spring.config.additional-location=classpath:/application-multiple-properties.yaml"
    })
class EndpointGateAutoConfigurationMultiplePropertiesTest {

  EndpointGateProperties endpointGateProperties;

  @Test
  void shouldLoadMultipleGates() {
    Map<String, Boolean> gates = endpointGateProperties.gateIds();
    assertEquals(2, gates.size());
    assertTrue(gates.get("g1"));
    assertFalse(gates.get("g2"));
  }

  @Test
  void shouldLoadRolloutPercentages() {
    Map<String, Integer> rolloutPercentages = endpointGateProperties.rolloutPercentages();
    assertEquals(2, rolloutPercentages.size());
    assertEquals(100, rolloutPercentages.get("g1"));
    assertEquals(100, rolloutPercentages.get("g2"));
  }

  @Test
  void shouldLoadDefaultEnabled() {
    assertTrue(endpointGateProperties.defaultEnabled());
  }

  @Test
  void shouldLoadResponseType() {
    ResponseProperties response = endpointGateProperties.response();
    assertEquals(ResponseType.PLAIN_TEXT, response.type());
  }

  @Autowired
  EndpointGateAutoConfigurationMultiplePropertiesTest(
      EndpointGateProperties endpointGateProperties) {
    this.endpointGateProperties = endpointGateProperties;
  }
}
