package net.brightroom.endpointgate.spring.webmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.brightroom.endpointgate.spring.webmvc.configuration.EndpointGateMvcTestAutoConfiguration;
import net.brightroom.endpointgate.spring.webmvc.endpoint.EndpointGateScheduleController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies schedule-based endpoint gate control through the full MVC stack:
 *
 * <ul>
 *   <li>Property configuration → {@code InMemoryScheduleProvider} auto-wiring → interceptor → HTTP
 *       response
 *   <li>Active schedule (start in the past) → 200 OK
 *   <li>Inactive schedule (start in the future) → 403 Forbidden
 *   <li>Timezone-aware schedule → correctly evaluated in the configured timezone
 * </ul>
 */
@WebMvcTest(controllers = EndpointGateScheduleController.class)
@Import(EndpointGateMvcTestAutoConfiguration.class)
@TestPropertySource(
    properties = {
      // active-scheduled-gate: start far in the past → always active
      "endpoint-gate.gates.active-scheduled-gate.enabled=true",
      "endpoint-gate.gates.active-scheduled-gate.schedule.start=2020-01-01T00:00:00",
      // inactive-scheduled-gate: start far in the future → always inactive
      "endpoint-gate.gates.inactive-scheduled-gate.enabled=true",
      "endpoint-gate.gates.inactive-scheduled-gate.schedule.start=2099-01-01T00:00:00",
      // timezone-scheduled-gate: start far in the past with timezone → always active
      "endpoint-gate.gates.timezone-scheduled-gate.enabled=true",
      "endpoint-gate.gates.timezone-scheduled-gate.schedule.start=2020-01-01T00:00:00",
      "endpoint-gate.gates.timezone-scheduled-gate.schedule.timezone=Asia/Tokyo",
    })
class EndpointGateInterceptorScheduleIntegrationTest {

  MockMvc mockMvc;

  @Autowired
  EndpointGateInterceptorScheduleIntegrationTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @Test
  void shouldAllowAccess_whenScheduleIsActive() throws Exception {
    mockMvc
        .perform(get("/schedule/active"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }

  @Test
  void shouldBlockAccess_whenScheduleIsInactive() throws Exception {
    mockMvc
        .perform(get("/schedule/inactive"))
        .andExpect(status().isForbidden())
        .andExpect(
            content()
                .json(
                    """
                    {
                      "detail" : "Gate 'inactive-scheduled-gate' is not available",
                      "instance" : "/schedule/inactive",
                      "status" : 403,
                      "title" : "Endpoint gate access denied",
                      "type" : "https://github.com/bright-room/endpoint-gate#response-types"
                    }
                    """));
  }

  @Test
  void shouldAllowAccess_whenScheduleIsActiveWithTimezone() throws Exception {
    mockMvc
        .perform(get("/schedule/timezone"))
        .andExpect(status().isOk())
        .andExpect(content().string("Allowed"));
  }
}
