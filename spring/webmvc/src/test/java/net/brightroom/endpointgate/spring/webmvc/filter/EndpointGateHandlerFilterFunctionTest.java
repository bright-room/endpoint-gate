package net.brightroom.endpointgate.spring.webmvc.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.brightroom.endpointgate.core.condition.EndpointGateConditionEvaluator;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.ConditionEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.EnabledEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.core.evaluation.RolloutEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.ScheduleEvaluationStep;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.provider.ConditionProvider;
import net.brightroom.endpointgate.core.provider.EndpointGateProvider;
import net.brightroom.endpointgate.core.provider.RolloutPercentageProvider;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.core.provider.ScheduleProvider;
import net.brightroom.endpointgate.core.rollout.DefaultRolloutStrategy;
import net.brightroom.endpointgate.core.rollout.RolloutStrategy;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webmvc.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

class EndpointGateHandlerFilterFunctionTest {

  private final EndpointGateProvider provider = mock(EndpointGateProvider.class);
  private final AccessDeniedHandlerFilterResolution resolution =
      mock(AccessDeniedHandlerFilterResolution.class);
  private final EndpointGateContextResolver contextResolver =
      mock(EndpointGateContextResolver.class);
  private final RolloutPercentageProvider rolloutPercentageProvider =
      mock(RolloutPercentageProvider.class);
  private final ConditionProvider conditionProvider =
      mock(ConditionProvider.class, invocation -> Optional.empty());
  private final EndpointGateConditionEvaluator conditionEvaluator =
      mock(EndpointGateConditionEvaluator.class);
  private final ScheduleProvider scheduleProvider =
      mock(ScheduleProvider.class, invocation -> Optional.empty());
  private final RolloutStrategy rolloutStrategy = mock(RolloutStrategy.class);

  private EndpointGateHandlerFilterFunction buildFilterFunction(RolloutStrategy strategy) {
    EndpointGateEvaluationPipeline pipeline =
        new EndpointGateEvaluationPipeline(
            new EnabledEvaluationStep(provider),
            new ScheduleEvaluationStep(scheduleProvider, Clock.systemDefaultZone()),
            new ConditionEvaluationStep(conditionEvaluator),
            new RolloutEvaluationStep(strategy));
    return new EndpointGateHandlerFilterFunction(
        pipeline, resolution, rolloutPercentageProvider, conditionProvider, contextResolver);
  }

  private final EndpointGateHandlerFilterFunction filterFunction =
      buildFilterFunction(new DefaultRolloutStrategy());
  private final EndpointGateHandlerFilterFunction filterFunctionWithRollout =
      buildFilterFunction(rolloutStrategy);

  // --- checkSchedule ---

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToResolution_whenScheduleIsInactive() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());
    Schedule inactiveSchedule = new Schedule(null, LocalDateTime.of(2020, 1, 1, 0, 0), null);
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.of(inactiveSchedule));

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    stubServletRequestForConditionVariables(httpServletRequest);
    ServerRequest request = mock(ServerRequest.class);
    when(request.servletRequest()).thenReturn(httpServletRequest);
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.empty());

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(deniedResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(deniedResponse);
    verifyNoInteractions(next);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenScheduleIsActive() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());
    Schedule activeSchedule = new Schedule(LocalDateTime.of(2020, 1, 1, 0, 0), null, null);
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.of(activeSchedule));

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    stubServletRequestForConditionVariables(httpServletRequest);
    ServerRequest request = mock(ServerRequest.class);
    when(request.servletRequest()).thenReturn(httpServletRequest);
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.empty());

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(okResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(okResponse);
    verify(next).handle(request);
  }

  @Test
  void of_throwsIllegalArgumentException_whenGateIdIsNull() {
    assertThatThrownBy(() -> filterFunction.of(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("null or blank");
  }

  @Test
  void of_throwsIllegalArgumentException_whenGateIdIsEmpty() {
    assertThatThrownBy(() -> filterFunction.of(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("null or blank");
  }

  @Test
  void of_throwsIllegalArgumentException_whenRolloutIsNegative() {
    assertThatThrownBy(() -> filterFunction.of("my-gate", -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("rollout must be between 0 and 100");
  }

  @Test
  void of_throwsIllegalArgumentException_whenRolloutIsOver100() {
    assertThatThrownBy(() -> filterFunction.of("my-gate", 101))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("rollout must be between 0 and 100");
  }

  private void stubServletRequest(ServerRequest serverRequest, HttpServletRequest httpRequest) {
    when(serverRequest.servletRequest()).thenReturn(httpRequest);
    stubServletRequestForConditionVariables(httpRequest);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenGateEnabled() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    ServerRequest request = mock(ServerRequest.class);
    stubServletRequest(request, httpRequest);
    when(contextResolver.resolve(httpRequest)).thenReturn(Optional.empty());

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(okResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(okResponse);
    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToResolution_whenGateDisabled() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(false);

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    ServerRequest request = mock(ServerRequest.class);
    stubServletRequest(request, httpRequest);
    when(contextResolver.resolve(httpRequest)).thenReturn(Optional.empty());

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(deniedResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(deniedResponse);
    verifyNoInteractions(next);
    verify(resolution).resolve(eq(request), any(EndpointGateAccessDeniedException.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenRolloutCheckPasses() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    ServerRequest request = mock(ServerRequest.class);
    stubServletRequest(request, httpServletRequest);

    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 50)).thenReturn(true);

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(okResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", 50);
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(okResponse);
    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToResolution_whenRolloutCheckFails() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    ServerRequest request = mock(ServerRequest.class);
    stubServletRequest(request, httpServletRequest);

    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 50)).thenReturn(false);

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(deniedResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", 50);
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(deniedResponse);
    verifyNoInteractions(next);
    verify(resolution).resolve(eq(request), any(EndpointGateAccessDeniedException.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenContextIsEmpty() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    ServerRequest request = mock(ServerRequest.class);
    stubServletRequest(request, httpServletRequest);
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.empty());

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(okResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", 50);
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(okResponse);
    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_usesProviderRollout_whenProviderReturnsValue() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.of(70));

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    ServerRequest request = mock(ServerRequest.class);
    stubServletRequest(request, httpServletRequest);

    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 70)).thenReturn(true);

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(okResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", 50);
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(okResponse);
    verify(rolloutStrategy).isInRollout("my-gate", context, 70);
    verify(next).handle(request);
  }

  private void stubServletRequestForConditionVariables(HttpServletRequest httpServletRequest) {
    when(httpServletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(httpServletRequest.getParameterMap()).thenReturn(Map.of());
    when(httpServletRequest.getCookies()).thenReturn(null);
    when(httpServletRequest.getRequestURI()).thenReturn("/functional/condition/header");
    when(httpServletRequest.getMethod()).thenReturn("GET");
    when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenConditionPasses() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    stubServletRequestForConditionVariables(httpServletRequest);

    ServerRequest request = mock(ServerRequest.class);
    when(request.servletRequest()).thenReturn(httpServletRequest);
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.empty());

    when(conditionEvaluator.evaluate(eq("headers['X-Beta'] != null"), any())).thenReturn(true);

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(okResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunction.of("my-gate", "headers['X-Beta'] != null");
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(okResponse);
    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToResolution_whenConditionFails() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    stubServletRequestForConditionVariables(httpServletRequest);

    ServerRequest request = mock(ServerRequest.class);
    when(request.servletRequest()).thenReturn(httpServletRequest);
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.empty());

    when(conditionEvaluator.evaluate(eq("headers['X-Beta'] != null"), any())).thenReturn(false);

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(deniedResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunction.of("my-gate", "headers['X-Beta'] != null");
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(deniedResponse);
    verifyNoInteractions(next);
    verify(resolution).resolve(eq(request), any(EndpointGateAccessDeniedException.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_skipsConditionCheck_whenConditionIsEmpty() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    stubServletRequestForConditionVariables(httpServletRequest);
    ServerRequest request = mock(ServerRequest.class);
    when(request.servletRequest()).thenReturn(httpServletRequest);
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.empty());

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(okResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    filter.filter(request, next);

    verifyNoInteractions(conditionEvaluator);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_usesFallbackRollout_whenProviderReturnsEmpty() throws Exception {
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    ServerRequest request = mock(ServerRequest.class);
    stubServletRequest(request, httpServletRequest);

    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(httpServletRequest)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 30)).thenReturn(false);

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(deniedResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", 30);
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(deniedResponse);
    verify(rolloutStrategy).isInRollout("my-gate", context, 30);
    verifyNoInteractions(next);
  }

  // --- pipeline delegation ---

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToPipeline() throws Exception {
    EndpointGateEvaluationPipeline pipeline = mock(EndpointGateEvaluationPipeline.class);
    EndpointGateHandlerFilterFunction filterFn =
        new EndpointGateHandlerFilterFunction(
            pipeline, resolution, rolloutPercentageProvider, conditionProvider, contextResolver);

    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    stubServletRequestForConditionVariables(httpRequest);
    ServerRequest request = mock(ServerRequest.class);
    when(request.servletRequest()).thenReturn(httpRequest);
    when(contextResolver.resolve(httpRequest)).thenReturn(Optional.empty());
    when(pipeline.evaluate(any())).thenReturn(AccessDecision.allowed());

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(okResponse);

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFn.of("my-gate");
    ServerResponse result = filter.filter(request, next);

    assertThat(result).isEqualTo(okResponse);
    verify(pipeline).evaluate(any());
  }
}
