package net.brightroom.endpointgate.spring.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResponsePropertiesTest {

  @Test
  void type_defaultsToJson() {
    var props = new ResponseProperties();

    assertEquals(ResponseType.JSON, props.type());
  }

  @Test
  void setType_updatesValue() {
    var props = new ResponseProperties();

    props.setType(ResponseType.HTML);

    assertEquals(ResponseType.HTML, props.type());
  }

  @Test
  void setType_plainText_updatesValue() {
    var props = new ResponseProperties();

    props.setType(ResponseType.PLAIN_TEXT);

    assertEquals(ResponseType.PLAIN_TEXT, props.type());
  }
}
