package net.brightroom.endpointgate.spring.core.resolution;

import static org.assertj.core.api.Assertions.assertThat;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import org.junit.jupiter.api.Test;

class HtmlResponseBuilderTest {

  @Test
  void buildHtml_containsDoctype() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    String result = HtmlResponseBuilder.buildHtml(e);
    assertThat(result).contains("<!DOCTYPE html>");
  }

  @Test
  void buildHtml_contains403InBody() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    String result = HtmlResponseBuilder.buildHtml(e);
    assertThat(result).contains("403");
  }

  @Test
  void buildHtml_containsGateId() {
    var e = new EndpointGateAccessDeniedException("my-gate");
    String result = HtmlResponseBuilder.buildHtml(e);
    assertThat(result).contains("my-gate");
  }

  @Test
  void buildHtml_escapesHtmlCharactersInMessage() {
    var e = new EndpointGateAccessDeniedException("<script>xss</script>");
    String result = HtmlResponseBuilder.buildHtml(e);
    assertThat(result).doesNotContain("<script>");
    assertThat(result).contains("&lt;script&gt;");
  }

  @Test
  void buildHtml_contains503InBody_whenScheduleInactiveException() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    String result = HtmlResponseBuilder.buildHtml(e);
    assertThat(result).contains("503");
  }

  @Test
  void buildHtml_containsTemporarilyUnavailableInBody_whenScheduleInactiveException() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    String result = HtmlResponseBuilder.buildHtml(e);
    assertThat(result).contains("Temporarily Unavailable");
  }

  @Test
  void buildHtml_doesNotContain403_whenScheduleInactiveException() {
    var e = new EndpointGateScheduleInactiveException("my-gate", null);
    String result = HtmlResponseBuilder.buildHtml(e);
    assertThat(result).doesNotContain("403");
  }
}
