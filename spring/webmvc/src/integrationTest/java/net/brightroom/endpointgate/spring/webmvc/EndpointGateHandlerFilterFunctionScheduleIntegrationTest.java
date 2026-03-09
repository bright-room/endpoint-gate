package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateScheduleRouterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies schedule-based endpoint gate control through the HandlerFilterFunction path:
 *
 * <ul>
 *   <li>Property configuration → {@code InMemoryScheduleProvider} auto-wiring → filter → HTTP
 *       response
 *   <li>Active schedule (start in the past) → 200 OK
 *   <li>Inactive schedule (start in the future) → 503 Service Unavailable + Retry-After header
 *   <li>Inactive schedule (end only, no start) → 503 Service Unavailable, no Retry-After header
 *   <li>Timezone-aware schedule → correctly evaluated in the configured timezone
 * </ul>
 */
@WebMvcTest
@Import({EndpointGateMvcTestAutoConfiguration.class, EndpointGateScheduleRouterConfiguration.class})
@TestPropertySource(
    properties = {
      // active-scheduled-gate: start far in the past → always active
      "endpoint-gate.gates.active-scheduled-gate.enabled=true",
      "endpoint-gate.gates.active-scheduled-gate.schedule.start=2020-01-01T00:00:00",
      // inactive-scheduled-gate: start far in the future → always inactive
      "endpoint-gate.gates.inactive-scheduled-gate.enabled=true",
      "endpoint-gate.gates.inactive-scheduled-gate.schedule.start=2099-01-01T00:00:00",
      // end-only-inactive-scheduled-gate: end in the past, no start → inactive, no retry-after
      "endpoint-gate.gates.end-only-inactive-scheduled-gate.enabled=true",
      "endpoint-gate.gates.end-only-inactive-scheduled-gate.schedule.end=2020-01-01T00:00:00",
      // timezone-scheduled-gate: start far in the past with timezone → always active
      "endpoint-gate.gates.timezone-scheduled-gate.enabled=true",
      "endpoint-gate.gates.timezone-scheduled-gate.schedule.start=2020-01-01T00:00:00",
      "endpoint-gate.gates.timezone-scheduled-gate.schedule.timezone=Asia/Tokyo",
    })
class EndpointGateHandlerFilterFunctionScheduleIntegrationTest {

  MockMvc mockMvc;

  @Autowired
  EndpointGateHandlerFilterFunctionScheduleIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @Test
  void shouldAllowAccess_whenScheduleIsActive() throws Exception {
    mockMvc
        .perform(get("/functional/schedule/active"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldReturn503_whenScheduleIsInactive() throws Exception {
    mockMvc
        .perform(get("/functional/schedule/inactive"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(
            content()
                .json(
                    """
                    {
                      "status" : 503,
                      "title" : "Endpoint gate temporarily unavailable",
                      "type" : "https://github.com/bright-room/endpoint-gate#response-types"
                    }
                    """));
  }

  @Test
  void shouldReturnRetryAfterHeader_whenScheduleIsInactive() throws Exception {
    mockMvc
        .perform(get("/functional/schedule/inactive"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(header().exists("Retry-After"))
        .andExpect(
            header()
                .string(
                    "Retry-After",
                    org.hamcrest.Matchers.matchesPattern(
                        "\\w{3}, \\d{1,2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} GMT")));
  }

  @Test
  void shouldReturn503WithoutRetryAfter_whenEndOnlyScheduleIsInactive() throws Exception {
    mockMvc
        .perform(get("/functional/schedule/end-only-inactive"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(header().doesNotExist("Retry-After"));
  }

  @Test
  void shouldAllowAccess_whenScheduleIsActiveWithTimezone() throws Exception {
    mockMvc
        .perform(get("/functional/schedule/timezone"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }
}
