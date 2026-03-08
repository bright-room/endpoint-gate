package net.brightroom.endpointgate.spring.webmvc.interceptor;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.brightroom.endpointgate.core.annotation.EndpointGate;
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
import net.brightroom.endpointgate.core.rollout.RolloutStrategy;
import net.brightroom.endpointgate.spring.webmvc.context.EndpointGateContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

class EndpointGateInterceptorTest {

  private final EndpointGateProvider provider = mock(EndpointGateProvider.class);
  private final RolloutStrategy rolloutStrategy = mock(RolloutStrategy.class);
  private final EndpointGateContextResolver contextResolver =
      mock(EndpointGateContextResolver.class);
  private final RolloutPercentageProvider rolloutPercentageProvider =
      mock(RolloutPercentageProvider.class);
  private final ConditionProvider conditionProvider = mock(ConditionProvider.class);
  private final EndpointGateConditionEvaluator conditionEvaluator =
      mock(EndpointGateConditionEvaluator.class);
  private final ScheduleProvider scheduleProvider =
      mock(ScheduleProvider.class, invocation -> Optional.empty());

  private EndpointGateInterceptor buildInterceptor() {
    EndpointGateEvaluationPipeline pipeline =
        new EndpointGateEvaluationPipeline(
            new EnabledEvaluationStep(provider),
            new ScheduleEvaluationStep(scheduleProvider, Clock.systemDefaultZone()),
            new ConditionEvaluationStep(conditionEvaluator),
            new RolloutEvaluationStep(rolloutStrategy));
    return new EndpointGateInterceptor(
        pipeline, rolloutPercentageProvider, conditionProvider, contextResolver);
  }

  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);

  @BeforeEach
  void setUp() {
    stubRequestForConditionVariables();
    when(rolloutPercentageProvider.getRolloutPercentage(any())).thenReturn(OptionalInt.empty());
    when(conditionProvider.getCondition(any())).thenReturn(Optional.empty());
    when(contextResolver.resolve(request)).thenReturn(Optional.empty());
  }

  private HandlerMethod handlerMethodWithAnnotation(EndpointGate annotation) {
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getMethodAnnotation(EndpointGate.class)).thenReturn(annotation);
    return handlerMethod;
  }

  private EndpointGate endpointGateAnnotation(String value) {
    EndpointGate annotation = mock(EndpointGate.class);
    when(annotation.value()).thenReturn(value);
    return annotation;
  }

  // --- checkSchedule ---

  @Test
  void preHandle_throwsEndpointGateAccessDeniedException_whenScheduleIsInactive() {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    Schedule inactiveSchedule = new Schedule(null, LocalDateTime.of(2020, 1, 1, 0, 0), null);
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.of(inactiveSchedule));

    assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
        .isInstanceOf(EndpointGateAccessDeniedException.class);
  }

  @Test
  void preHandle_returnsTrue_whenScheduleIsActive() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());
    Schedule activeSchedule = new Schedule(LocalDateTime.of(2020, 1, 1, 0, 0), null, null);
    when(scheduleProvider.getSchedule("my-gate")).thenReturn(Optional.of(activeSchedule));

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  // --- validateAnnotation ---

  @Test
  void preHandle_throwsIllegalStateException_whenEndpointGateValueIsEmpty() {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);

    assertThatIllegalStateException()
        .isThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
        .withMessageContaining("non-empty value");
  }

  // --- checkRollout ---

  @Test
  void preHandle_returnsTrue_whenRolloutIs100() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void preHandle_returnsTrue_whenContextPresentAndInsideRollout() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.of(50));
    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(request)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 50)).thenReturn(true);

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void preHandle_throwsEndpointGateAccessDeniedException_whenContextPresentAndOutsideRollout() {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.of(50));
    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(request)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 50)).thenReturn(false);

    assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
        .isInstanceOf(EndpointGateAccessDeniedException.class);
  }

  @Test
  void preHandle_returnsTrue_whenContextIsEmpty() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.of(50));
    when(contextResolver.resolve(request)).thenReturn(Optional.empty());

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void preHandle_returnsTrue_whenHandlerIsNotHandlerMethod() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    Object notAHandlerMethod = new Object();
    boolean result = interceptor.preHandle(request, response, notAHandlerMethod);
    assertTrue(result);
  }

  @Test
  void preHandle_returnsTrue_whenNoAnnotationOnMethodOrClass() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getMethodAnnotation(EndpointGate.class)).thenReturn(null);
    when(handlerMethod.getBeanType()).thenAnswer(inv -> Object.class);

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  // --- rollout=0 boundary ---

  @Test
  void preHandle_throwsEndpointGateAccessDeniedException_whenRolloutIsZero() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.of(0));
    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(request)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 0)).thenReturn(false);

    assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
        .isInstanceOf(EndpointGateAccessDeniedException.class);
  }

  // --- class-level @EndpointGate + rollout ---

  @EndpointGate("my-gate")
  static class RolloutAnnotatedController {}

  private HandlerMethod handlerMethodWithClassAnnotation() {
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getMethodAnnotation(EndpointGate.class)).thenReturn(null);
    when(handlerMethod.getBeanType()).thenAnswer(inv -> RolloutAnnotatedController.class);
    return handlerMethod;
  }

  @Test
  void preHandle_returnsTrue_whenClassAnnotationWithRolloutAndContextInsideRollout()
      throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    HandlerMethod handlerMethod = handlerMethodWithClassAnnotation();
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.of(50));
    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(request)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 50)).thenReturn(true);

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void
      preHandle_throwsEndpointGateAccessDeniedException_whenClassAnnotationWithRolloutAndContextOutsideRollout() {
    EndpointGateInterceptor interceptor = buildInterceptor();
    HandlerMethod handlerMethod = handlerMethodWithClassAnnotation();
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.of(50));
    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(request)).thenReturn(Optional.of(context));
    when(rolloutStrategy.isInRollout("my-gate", context, 50)).thenReturn(false);

    assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
        .isInstanceOf(EndpointGateAccessDeniedException.class);
  }

  // --- condition ---

  private void stubRequestForConditionVariables() {
    when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(request.getParameterMap()).thenReturn(Map.of());
    when(request.getCookies()).thenReturn(null);
    when(request.getRequestURI()).thenReturn("/test");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
  }

  @Test
  void preHandle_returnsTrue_whenConditionIsTrue() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());
    when(conditionProvider.getCondition("my-gate"))
        .thenReturn(Optional.of("headers['X-Beta'] != null"));
    stubRequestForConditionVariables();
    when(conditionEvaluator.evaluate(
            org.mockito.ArgumentMatchers.eq("headers['X-Beta'] != null"),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(true);

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void preHandle_throwsEndpointGateAccessDeniedException_whenConditionIsFalse() {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(conditionProvider.getCondition("my-gate"))
        .thenReturn(Optional.of("headers['X-Beta'] != null"));
    stubRequestForConditionVariables();
    when(conditionEvaluator.evaluate(
            org.mockito.ArgumentMatchers.eq("headers['X-Beta'] != null"),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(false);

    assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
        .isInstanceOf(EndpointGateAccessDeniedException.class);
  }

  @Test
  void preHandle_skipsConditionCheck_whenConditionIsEmpty() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());
    when(conditionProvider.getCondition("my-gate")).thenReturn(Optional.empty());

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void preHandle_evaluatesConditionBeforeRollout() throws Exception {
    EndpointGateInterceptor interceptor = buildInterceptor();
    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(provider.isGateEnabled("my-gate")).thenReturn(true);
    when(conditionProvider.getCondition("my-gate"))
        .thenReturn(Optional.of("headers['X-Beta'] != null"));
    stubRequestForConditionVariables();
    when(conditionEvaluator.evaluate(
            org.mockito.ArgumentMatchers.eq("headers['X-Beta'] != null"),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(false);

    assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
        .isInstanceOf(EndpointGateAccessDeniedException.class);
  }

  // --- pipeline delegation ---

  @Test
  void preHandle_delegatesToPipeline_whenDecisionIsAllowed() throws Exception {
    EndpointGateEvaluationPipeline pipeline = mock(EndpointGateEvaluationPipeline.class);
    EndpointGateInterceptor interceptor =
        new EndpointGateInterceptor(
            pipeline, rolloutPercentageProvider, conditionProvider, contextResolver);

    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());
    when(conditionProvider.getCondition("my-gate")).thenReturn(Optional.empty());
    when(contextResolver.resolve(request)).thenReturn(Optional.empty());
    when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(request.getParameterMap()).thenReturn(Map.of());
    when(request.getCookies()).thenReturn(null);
    when(request.getRequestURI()).thenReturn("/test");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(pipeline.evaluate(org.mockito.ArgumentMatchers.any()))
        .thenReturn(AccessDecision.allowed());

    boolean result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void preHandle_throwsException_whenPipelineReturnsDenied() {
    EndpointGateEvaluationPipeline pipeline = mock(EndpointGateEvaluationPipeline.class);
    EndpointGateInterceptor interceptor =
        new EndpointGateInterceptor(
            pipeline, rolloutPercentageProvider, conditionProvider, contextResolver);

    EndpointGate annotation = endpointGateAnnotation("my-gate");
    HandlerMethod handlerMethod = handlerMethodWithAnnotation(annotation);
    when(rolloutPercentageProvider.getRolloutPercentage("my-gate")).thenReturn(OptionalInt.empty());
    when(conditionProvider.getCondition("my-gate")).thenReturn(Optional.empty());
    when(contextResolver.resolve(request)).thenReturn(Optional.empty());
    when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(request.getParameterMap()).thenReturn(Map.of());
    when(request.getCookies()).thenReturn(null);
    when(request.getRequestURI()).thenReturn("/test");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(pipeline.evaluate(org.mockito.ArgumentMatchers.any()))
        .thenReturn(AccessDecision.denied("my-gate", AccessDecision.DeniedReason.DISABLED));

    assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
        .isInstanceOf(EndpointGateAccessDeniedException.class);
  }
}
