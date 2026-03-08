package net.brightroom.endpointgate.spring.webflux.filter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.reactive.core.condition.ReactiveEndpointGateConditionEvaluator;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveConditionEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEnabledEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveRolloutEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveScheduleEvaluationStep;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveConditionProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveEndpointGateProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveRolloutPercentageProvider;
import net.brightroom.endpointgate.reactive.core.provider.ReactiveScheduleProvider;
import net.brightroom.endpointgate.reactive.core.rollout.DefaultReactiveRolloutStrategy;
import net.brightroom.endpointgate.reactive.core.rollout.ReactiveRolloutStrategy;
import net.brightroom.endpointgate.spring.webflux.context.ReactiveEndpointGateContextResolver;
import net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class EndpointGateHandlerFilterFunctionTest {

  private final ReactiveEndpointGateProvider provider = mock(ReactiveEndpointGateProvider.class);
  private final AccessDeniedHandlerFilterResolution resolution =
      mock(AccessDeniedHandlerFilterResolution.class);
  private final ReactiveEndpointGateContextResolver contextResolver =
      mock(ReactiveEndpointGateContextResolver.class, invocation -> Mono.empty());
  private final ReactiveRolloutPercentageProvider rolloutPercentageProvider =
      mock(ReactiveRolloutPercentageProvider.class, invocation -> Mono.empty());
  private final ReactiveConditionProvider conditionProvider =
      mock(ReactiveConditionProvider.class, invocation -> Mono.empty());
  private final ReactiveEndpointGateConditionEvaluator conditionEvaluator =
      mock(ReactiveEndpointGateConditionEvaluator.class);
  private final ReactiveScheduleProvider reactiveScheduleProvider =
      mock(ReactiveScheduleProvider.class, invocation -> Mono.empty());
  private final ReactiveRolloutStrategy rolloutStrategy = mock(ReactiveRolloutStrategy.class);

  private ReactiveEndpointGateEvaluationPipeline buildPipeline(ReactiveRolloutStrategy strategy) {
    return new ReactiveEndpointGateEvaluationPipeline(
        new ReactiveEnabledEvaluationStep(provider),
        new ReactiveScheduleEvaluationStep(reactiveScheduleProvider, Clock.systemDefaultZone()),
        new ReactiveConditionEvaluationStep(conditionEvaluator),
        new ReactiveRolloutEvaluationStep(strategy));
  }

  private final EndpointGateHandlerFilterFunction filterFunction =
      new EndpointGateHandlerFilterFunction(
          buildPipeline(new DefaultReactiveRolloutStrategy()),
          resolution,
          rolloutPercentageProvider,
          conditionProvider,
          contextResolver);

  private final EndpointGateHandlerFilterFunction filterFunctionWithRollout =
      new EndpointGateHandlerFilterFunction(
          buildPipeline(rolloutStrategy),
          resolution,
          rolloutPercentageProvider,
          conditionProvider,
          contextResolver);

  private ServerRequest mockRequest() {
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(httpRequest.getHeaders()).thenReturn(new HttpHeaders());
    when(httpRequest.getQueryParams()).thenReturn(new LinkedMultiValueMap<>());
    when(httpRequest.getCookies()).thenReturn(new LinkedMultiValueMap<>());
    org.springframework.http.server.RequestPath path =
        mock(org.springframework.http.server.RequestPath.class);
    when(path.value()).thenReturn("/test");
    when(httpRequest.getPath()).thenReturn(path);
    when(httpRequest.getMethod()).thenReturn(HttpMethod.GET);
    when(httpRequest.getRemoteAddress()).thenReturn(null);
    ServerWebExchange exchange = mock(ServerWebExchange.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    ServerRequest request = mock(ServerRequest.class);
    when(request.exchange()).thenReturn(exchange);
    return request;
  }

  private ServerRequest mockRequest(ServerHttpRequest httpRequest) {
    ServerWebExchange exchange = mock(ServerWebExchange.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    ServerRequest request = mock(ServerRequest.class);
    when(request.exchange()).thenReturn(exchange);
    return request;
  }

  private ServerHttpRequest mockHttpRequest() {
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(httpRequest.getHeaders()).thenReturn(new HttpHeaders());
    when(httpRequest.getQueryParams()).thenReturn(new LinkedMultiValueMap<>());
    when(httpRequest.getCookies()).thenReturn(new LinkedMultiValueMap<>());
    org.springframework.http.server.RequestPath path =
        mock(org.springframework.http.server.RequestPath.class);
    when(path.value()).thenReturn("/functional/condition/header");
    when(httpRequest.getPath()).thenReturn(path);
    when(httpRequest.getMethod()).thenReturn(HttpMethod.GET);
    when(httpRequest.getRemoteAddress()).thenReturn(null);
    return httpRequest;
  }

  // --- validation ---

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

  // --- enabled check ---

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenGateEnabled() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    ServerRequest request = mockRequest();
    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(Mono.just(okResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    StepVerifier.create(filter.filter(request, next)).expectNext(okResponse).verifyComplete();

    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToResolution_whenGateDisabled() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(false));
    ServerRequest request = mockRequest();
    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(Mono.just(deniedResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    StepVerifier.create(filter.filter(request, next)).expectNext(deniedResponse).verifyComplete();

    verifyNoInteractions(next);
    verify(resolution).resolve(eq(request), any(EndpointGateAccessDeniedException.class));
  }

  // --- schedule check ---

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToResolution_whenScheduleIsInactive() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    Schedule inactiveSchedule = new Schedule(null, LocalDateTime.of(2020, 1, 1, 0, 0), null);
    when(reactiveScheduleProvider.getSchedule("my-gate")).thenReturn(Mono.just(inactiveSchedule));

    ServerRequest request = mockRequest();
    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(Mono.just(deniedResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    StepVerifier.create(filter.filter(request, next)).expectNext(deniedResponse).verifyComplete();

    verifyNoInteractions(next);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenScheduleIsActive() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    Schedule activeSchedule = new Schedule(LocalDateTime.of(2020, 1, 1, 0, 0), null, null);
    when(reactiveScheduleProvider.getSchedule("my-gate")).thenReturn(Mono.just(activeSchedule));

    ServerRequest request = mockRequest();
    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(Mono.just(okResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    StepVerifier.create(filter.filter(request, next)).expectNext(okResponse).verifyComplete();

    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }

  // --- condition check ---

  @Test
  @SuppressWarnings("unchecked")
  void of_skipsConditionCheck_whenConditionIsEmpty() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    ServerRequest request = mockRequest();
    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(Mono.just(okResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter = filterFunction.of("my-gate");
    StepVerifier.create(filter.filter(request, next)).expectNext(okResponse).verifyComplete();

    verifyNoInteractions(conditionEvaluator);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenConditionPasses() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    ServerHttpRequest httpRequest = mockHttpRequest();
    ServerRequest request = mockRequest(httpRequest);

    when(conditionEvaluator.evaluate(eq("headers['X-Beta'] != null"), any()))
        .thenReturn(Mono.just(true));

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(Mono.just(okResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunction.of("my-gate", "headers['X-Beta'] != null");
    StepVerifier.create(filter.filter(request, next)).expectNext(okResponse).verifyComplete();

    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToResolution_whenConditionFails() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    ServerHttpRequest httpRequest = mockHttpRequest();
    ServerRequest request = mockRequest(httpRequest);

    when(conditionEvaluator.evaluate(eq("headers['X-Beta'] != null"), any()))
        .thenReturn(Mono.just(false));

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(Mono.just(deniedResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunction.of("my-gate", "headers['X-Beta'] != null");
    StepVerifier.create(filter.filter(request, next)).expectNext(deniedResponse).verifyComplete();

    verifyNoInteractions(next);
    verify(resolution).resolve(eq(request), any(EndpointGateAccessDeniedException.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_evaluatesConditionBeforeRollout() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    ServerHttpRequest httpRequest = mockHttpRequest();
    ServerRequest request = mockRequest(httpRequest);

    when(conditionEvaluator.evaluate(eq("headers['X-Beta'] != null"), any()))
        .thenReturn(Mono.just(false));

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(Mono.just(deniedResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", "headers['X-Beta'] != null", 50);
    StepVerifier.create(filter.filter(request, next)).expectNext(deniedResponse).verifyComplete();

    verifyNoInteractions(rolloutStrategy);
  }

  // --- rollout check ---

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenRolloutCheckPasses() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    ServerHttpRequest httpRequest = mockHttpRequest();
    EndpointGateContext context = new EndpointGateContext("u1");
    when(contextResolver.resolve(httpRequest)).thenReturn(Mono.just(context));
    ServerRequest request = mockRequest(httpRequest);

    when(rolloutStrategy.isInRollout("my-gate", context, 50)).thenReturn(Mono.just(true));

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(Mono.just(okResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", 50);
    StepVerifier.create(filter.filter(request, next)).expectNext(okResponse).verifyComplete();

    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToResolution_whenRolloutCheckFails() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    ServerHttpRequest httpRequest = mockHttpRequest();
    EndpointGateContext context = new EndpointGateContext("u1");
    when(contextResolver.resolve(httpRequest)).thenReturn(Mono.just(context));
    ServerRequest request = mockRequest(httpRequest);

    when(rolloutStrategy.isInRollout("my-gate", context, 50)).thenReturn(Mono.just(false));

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse deniedResponse = mock(ServerResponse.class);
    when(resolution.resolve(eq(request), any(EndpointGateAccessDeniedException.class)))
        .thenReturn(Mono.just(deniedResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", 50);
    StepVerifier.create(filter.filter(request, next)).expectNext(deniedResponse).verifyComplete();

    verifyNoInteractions(next);
    verify(resolution).resolve(eq(request), any(EndpointGateAccessDeniedException.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void of_delegatesToNext_whenContextIsEmpty() {
    when(provider.isGateEnabled("my-gate")).thenReturn(Mono.just(true));
    ServerRequest request = mockRequest();

    HandlerFunction<ServerResponse> next = mock(HandlerFunction.class);
    ServerResponse okResponse = mock(ServerResponse.class);
    when(next.handle(request)).thenReturn(Mono.just(okResponse));

    HandlerFilterFunction<ServerResponse, ServerResponse> filter =
        filterFunctionWithRollout.of("my-gate", 50);
    StepVerifier.create(filter.filter(request, next)).expectNext(okResponse).verifyComplete();

    verify(next).handle(request);
    verifyNoInteractions(resolution);
  }
}
