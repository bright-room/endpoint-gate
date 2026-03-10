package net.brightroom.endpointgate.spring.webflux.aspect;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDateTime;
import net.brightroom.endpointgate.core.annotation.EndpointGate;
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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class EndpointGateAspectTest {

  private final ReactiveEndpointGateProvider provider = mock(ReactiveEndpointGateProvider.class);
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

  private final EndpointGateAspect aspect =
      new EndpointGateAspect(
          buildPipeline(new DefaultReactiveRolloutStrategy()),
          contextResolver,
          rolloutPercentageProvider,
          conditionProvider);

  private final EndpointGateAspect aspectWithRollout =
      new EndpointGateAspect(
          buildPipeline(rolloutStrategy),
          contextResolver,
          rolloutPercentageProvider,
          conditionProvider);

  private ServerWebExchange mockExchange() {
    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);
    return exchange;
  }

  static class TestController {

    @EndpointGate("")
    public Mono<String> emptyAnnotationMethod() {
      return Mono.just("ok");
    }

    @EndpointGate({})
    public Mono<String> emptyArrayAnnotationMethod() {
      return Mono.just("ok");
    }

    @EndpointGate({"gate-a", "gate-b"})
    public Mono<String> multiGateMonoMethod() {
      return Mono.just("multi-gate-result");
    }

    @EndpointGate("some-feature")
    public String nonReactiveMethod() {
      return "non-reactive";
    }

    @EndpointGate("some-feature")
    public Mono<String> monoMethod() {
      return Mono.just("result");
    }

    @EndpointGate("some-feature")
    public Flux<String> fluxMethod() {
      return Flux.just("result1", "result2");
    }

    @EndpointGate("some-feature")
    public Mono<String> rolloutMonoMethod() {
      return Mono.just("result");
    }

    @EndpointGate("some-feature")
    public Flux<String> rolloutFluxMethod() {
      return Flux.just("result1", "result2");
    }

    @EndpointGate("some-feature")
    public Mono<String> conditionMonoMethod() {
      return Mono.just("result");
    }

    @EndpointGate("some-feature")
    public Flux<String> conditionFluxMethod() {
      return Flux.just("result1", "result2");
    }
  }

  static class NoAnnotationController {

    public Mono<String> noAnnotationMethod() {
      return Mono.just("no-annotation");
    }
  }

  // --- checkSchedule for Mono ---

  @Test
  @SuppressWarnings("unchecked")
  void checkEndpointGate_emitsError_whenScheduleIsInactive_forMono() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("monoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    Schedule inactiveSchedule = new Schedule(null, LocalDateTime.of(2020, 1, 1, 0, 0), null);
    when(reactiveScheduleProvider.getSchedule("some-feature"))
        .thenReturn(Mono.just(inactiveSchedule));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Mono<Object>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectError(EndpointGateAccessDeniedException.class)
        .verify();
  }

  @Test
  @SuppressWarnings("unchecked")
  void checkEndpointGate_proceeds_whenScheduleIsActive_forMono() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("monoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    Schedule activeSchedule = new Schedule(LocalDateTime.of(2020, 1, 1, 0, 0), null, null);
    when(reactiveScheduleProvider.getSchedule("some-feature"))
        .thenReturn(Mono.just(activeSchedule));
    when(joinPoint.proceed()).thenReturn(Mono.just("result"));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Mono<Object>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result")
        .verifyComplete();
  }

  // --- checkSchedule for Flux ---

  @Test
  @SuppressWarnings("unchecked")
  void checkEndpointGate_emitsError_whenScheduleIsInactive_forFlux() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("fluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    Schedule inactiveSchedule = new Schedule(null, LocalDateTime.of(2020, 1, 1, 0, 0), null);
    when(reactiveScheduleProvider.getSchedule("some-feature"))
        .thenReturn(Mono.just(inactiveSchedule));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Flux<Object>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectError(EndpointGateAccessDeniedException.class)
        .verify();
  }

  @Test
  @SuppressWarnings("unchecked")
  void checkEndpointGate_proceeds_whenScheduleIsActive_forFlux() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("fluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    Schedule activeSchedule = new Schedule(LocalDateTime.of(2020, 1, 1, 0, 0), null, null);
    when(reactiveScheduleProvider.getSchedule("some-feature"))
        .thenReturn(Mono.just(activeSchedule));
    when(joinPoint.proceed()).thenReturn(Flux.just("r1", "r2"));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Flux<Object>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("r1", "r2")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_throwsIllegalArgumentException_whenAnnotationValueIsEmpty()
      throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("emptyAnnotationMethod");
    when(signature.getMethod()).thenReturn(method);
    when(joinPoint.getTarget()).thenReturn(new TestController());

    assertThatThrownBy(() -> aspect.checkEndpointGate(joinPoint))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("null or blank");
  }

  @Test
  void checkEndpointGate_throwsIllegalArgumentException_whenAnnotationValueIsEmptyArray()
      throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("emptyArrayAnnotationMethod");
    when(signature.getMethod()).thenReturn(method);
    when(joinPoint.getTarget()).thenReturn(new TestController());

    assertThatThrownBy(() -> aspect.checkEndpointGate(joinPoint))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("null or empty");
  }

  // --- multipleGates ---

  @Test
  @SuppressWarnings("unchecked")
  void checkEndpointGate_returnsMono_whenAllGatesEnabled() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("multiGateMonoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("gate-a")).thenReturn(Mono.just(true));
    when(provider.isGateEnabled("gate-b")).thenReturn(Mono.just(true));
    when(joinPoint.proceed()).thenReturn(Mono.just("multi-gate-result"));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    Mono<String> mono = (Mono<String>) result;
    StepVerifier.create(mono.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("multi-gate-result")
        .verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void checkEndpointGate_returnsMonoError_whenSecondGateDisabled() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("multiGateMonoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("gate-a")).thenReturn(Mono.just(true));
    when(provider.isGateEnabled("gate-b")).thenReturn(Mono.just(false));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Mono<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(
            e ->
                e instanceof EndpointGateAccessDeniedException
                    && ((EndpointGateAccessDeniedException) e).gateId().equals("gate-b"))
        .verify();
  }

  @Test
  @SuppressWarnings("unchecked")
  void checkEndpointGate_returnsMonoError_whenFirstGateDisabled() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("multiGateMonoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("gate-a")).thenReturn(Mono.just(false));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Mono<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(
            e ->
                e instanceof EndpointGateAccessDeniedException
                    && ((EndpointGateAccessDeniedException) e).gateId().equals("gate-a"))
        .verify();
  }

  @Test
  void checkEndpointGate_throwsIllegalStateException_whenReturnTypeIsNonReactive()
      throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("nonReactiveMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(String.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());

    assertThatThrownBy(() -> aspect.checkEndpointGate(joinPoint))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("reactive return type");
  }

  @Test
  void checkEndpointGate_proceedsWithoutCheck_whenNoAnnotationFound() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = NoAnnotationController.class.getMethod("noAnnotationMethod");
    when(signature.getMethod()).thenReturn(method);
    when(joinPoint.getTarget()).thenReturn(new NoAnnotationController());
    when(joinPoint.proceed()).thenReturn(Mono.just("result"));

    aspect.checkEndpointGate(joinPoint);

    verify(joinPoint).proceed();
    verifyNoInteractions(provider);
  }

  @Test
  void checkEndpointGate_returnsMono_whenGateEnabled() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("monoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(joinPoint.proceed()).thenReturn(Mono.just("result"));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Mono<String> mono = (Mono<String>) result;
    StepVerifier.create(mono.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_returnsMonoError_whenGateDisabled() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("monoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(false));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Mono<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(
            e ->
                e instanceof EndpointGateAccessDeniedException
                    && ((EndpointGateAccessDeniedException) e).gateId().equals("some-feature"))
        .verify();
  }

  @Test
  void checkEndpointGate_returnsMonoError_whenRolloutCheckFails() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("rolloutMonoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(rolloutPercentageProvider.getRolloutPercentage("some-feature")).thenReturn(Mono.just(50));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);

    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(httpRequest)).thenReturn(Mono.just(context));
    when(rolloutStrategy.isInRollout("some-feature", context, 50)).thenReturn(Mono.just(false));

    Object result = aspectWithRollout.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Mono<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(
            e ->
                e instanceof EndpointGateAccessDeniedException
                    && ((EndpointGateAccessDeniedException) e).gateId().equals("some-feature"))
        .verify();
  }

  @Test
  void checkEndpointGate_returnsMono_whenRolloutCheckPasses() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("rolloutMonoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(rolloutPercentageProvider.getRolloutPercentage("some-feature")).thenReturn(Mono.just(50));
    when(joinPoint.proceed()).thenReturn(Mono.just("result"));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);

    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(httpRequest)).thenReturn(Mono.just(context));
    when(rolloutStrategy.isInRollout("some-feature", context, 50)).thenReturn(Mono.just(true));

    Object result = aspectWithRollout.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Mono<String> mono = (Mono<String>) result;
    StepVerifier.create(mono.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_returnsMono_whenContextIsEmpty() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("rolloutMonoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(rolloutPercentageProvider.getRolloutPercentage("some-feature")).thenReturn(Mono.just(50));
    when(joinPoint.proceed()).thenReturn(Mono.just("result"));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);
    when(contextResolver.resolve(httpRequest)).thenReturn(Mono.empty());

    Object result = aspectWithRollout.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Mono<String> mono = (Mono<String>) result;
    StepVerifier.create(mono.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_returnsFluxError_whenRolloutCheckFails() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("rolloutFluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(rolloutPercentageProvider.getRolloutPercentage("some-feature")).thenReturn(Mono.just(50));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);

    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(httpRequest)).thenReturn(Mono.just(context));
    when(rolloutStrategy.isInRollout("some-feature", context, 50)).thenReturn(Mono.just(false));

    Object result = aspectWithRollout.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Flux<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(
            e ->
                e instanceof EndpointGateAccessDeniedException
                    && ((EndpointGateAccessDeniedException) e).gateId().equals("some-feature"))
        .verify();
  }

  @Test
  void checkEndpointGate_returnsFlux_whenRolloutCheckPasses() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("rolloutFluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(rolloutPercentageProvider.getRolloutPercentage("some-feature")).thenReturn(Mono.just(50));
    when(joinPoint.proceed()).thenReturn(Flux.just("result1", "result2"));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);

    EndpointGateContext context = new EndpointGateContext("user-1");
    when(contextResolver.resolve(httpRequest)).thenReturn(Mono.just(context));
    when(rolloutStrategy.isInRollout("some-feature", context, 50)).thenReturn(Mono.just(true));

    Object result = aspectWithRollout.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Flux<String> flux = (Flux<String>) result;
    StepVerifier.create(flux.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result1", "result2")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_returnsFlux_whenContextIsEmpty() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("rolloutFluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(rolloutPercentageProvider.getRolloutPercentage("some-feature")).thenReturn(Mono.just(50));
    when(joinPoint.proceed()).thenReturn(Flux.just("result1", "result2"));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);
    when(contextResolver.resolve(httpRequest)).thenReturn(Mono.empty());

    Object result = aspectWithRollout.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Flux<String> flux = (Flux<String>) result;
    StepVerifier.create(flux.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result1", "result2")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_returnsFlux_whenGateEnabled() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("fluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(joinPoint.proceed()).thenReturn(Flux.just("result1", "result2"));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Flux<String> flux = (Flux<String>) result;
    StepVerifier.create(flux.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result1", "result2")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_returnsMonoError_whenProceedThrows() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("monoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    RuntimeException cause = new RuntimeException("unexpected");
    when(joinPoint.proceed()).thenThrow(cause);

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Mono<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(e -> e == cause)
        .verify();
  }

  @Test
  void checkEndpointGate_returnsFluxError_whenGateDisabled() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("fluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(false));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Flux<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(
            e ->
                e instanceof EndpointGateAccessDeniedException
                    && ((EndpointGateAccessDeniedException) e).gateId().equals("some-feature"))
        .verify();
  }

  // --- condition ---

  private void stubRequestForConditionVariables(ServerHttpRequest httpRequest) {
    when(httpRequest.getHeaders()).thenReturn(new HttpHeaders());
    when(httpRequest.getQueryParams()).thenReturn(new LinkedMultiValueMap<>());
    when(httpRequest.getCookies()).thenReturn(new LinkedMultiValueMap<>());
    org.springframework.http.server.RequestPath path =
        mock(org.springframework.http.server.RequestPath.class);
    when(path.value()).thenReturn("/test");
    when(httpRequest.getPath()).thenReturn(path);
    when(httpRequest.getMethod()).thenReturn(HttpMethod.GET);
    when(httpRequest.getRemoteAddress()).thenReturn(null);
  }

  @Test
  void checkEndpointGate_returnsMono_whenConditionIsTrue() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("conditionMonoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(conditionProvider.getCondition("some-feature"))
        .thenReturn(Mono.just("headers['X-Beta'] != null"));
    when(joinPoint.proceed()).thenReturn(Mono.just("result"));
    when(conditionEvaluator.evaluate(
            org.mockito.ArgumentMatchers.eq("headers['X-Beta'] != null"),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(Mono.just(true));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);

    Object result = aspect.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Mono<String> mono = (Mono<String>) result;
    StepVerifier.create(mono.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_returnsMonoError_whenConditionIsFalse() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("conditionMonoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(conditionProvider.getCondition("some-feature"))
        .thenReturn(Mono.just("headers['X-Beta'] != null"));
    when(conditionEvaluator.evaluate(
            org.mockito.ArgumentMatchers.eq("headers['X-Beta'] != null"),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(Mono.just(false));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);

    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Mono<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(
            e ->
                e instanceof EndpointGateAccessDeniedException
                    && ((EndpointGateAccessDeniedException) e).gateId().equals("some-feature"))
        .verify();
  }

  @Test
  void checkEndpointGate_returnsFlux_whenConditionIsTrue() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("conditionFluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(conditionProvider.getCondition("some-feature"))
        .thenReturn(Mono.just("headers['X-Beta'] != null"));
    when(joinPoint.proceed()).thenReturn(Flux.just("result1", "result2"));
    when(conditionEvaluator.evaluate(
            org.mockito.ArgumentMatchers.eq("headers['X-Beta'] != null"),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(Mono.just(true));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);

    Object result = aspect.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Flux<String> flux = (Flux<String>) result;
    StepVerifier.create(flux.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNext("result1", "result2")
        .verifyComplete();
  }

  @Test
  void checkEndpointGate_returnsFluxError_whenConditionIsFalse() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("conditionFluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(conditionProvider.getCondition("some-feature"))
        .thenReturn(Mono.just("headers['X-Beta'] != null"));
    when(conditionEvaluator.evaluate(
            org.mockito.ArgumentMatchers.eq("headers['X-Beta'] != null"),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(Mono.just(false));

    ServerWebExchange exchange = mock(ServerWebExchange.class);
    ServerHttpRequest httpRequest = mock(ServerHttpRequest.class);
    when(exchange.getRequest()).thenReturn(httpRequest);
    stubRequestForConditionVariables(httpRequest);

    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Flux<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(
            e ->
                e instanceof EndpointGateAccessDeniedException
                    && ((EndpointGateAccessDeniedException) e).gateId().equals("some-feature"))
        .verify();
  }

  @Test
  void checkEndpointGate_skipsConditionCheck_whenConditionIsEmpty() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("monoMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Mono.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    when(joinPoint.proceed()).thenReturn(Mono.just("result"));

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    @SuppressWarnings("unchecked")
    Mono<Object> mono = (Mono<Object>) result;
    StepVerifier.create(mono.contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectNextCount(1)
        .verifyComplete();
    verifyNoInteractions(conditionEvaluator);
  }

  @Test
  void checkEndpointGate_returnsFluxError_whenProceedThrows() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);

    Method method = TestController.class.getMethod("fluxMethod");
    when(signature.getMethod()).thenReturn(method);
    when(signature.getReturnType()).thenReturn(Flux.class);
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(provider.isGateEnabled("some-feature")).thenReturn(Mono.just(true));
    RuntimeException cause = new RuntimeException("unexpected");
    when(joinPoint.proceed()).thenThrow(cause);

    ServerWebExchange exchange = mockExchange();
    Object result = aspect.checkEndpointGate(joinPoint);

    StepVerifier.create(
            ((Flux<?>) result).contextWrite(ctx -> ctx.put(ServerWebExchange.class, exchange)))
        .expectErrorMatches(e -> e == cause)
        .verify();
  }
}
