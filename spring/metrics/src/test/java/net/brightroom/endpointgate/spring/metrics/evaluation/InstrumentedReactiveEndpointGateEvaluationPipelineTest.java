package net.brightroom.endpointgate.spring.metrics.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.core.provider.Schedule;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveConditionEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEnabledEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveRolloutEvaluationStep;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveScheduleEvaluationStep;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class InstrumentedReactiveEndpointGateEvaluationPipelineTest {

  private static final String GATE_ID = "test-gate";

  @Test
  void shouldRecordAllowedCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    ReactiveEndpointGateEvaluationPipeline delegate = createAllowingPipeline();
    InstrumentedReactiveEndpointGateEvaluationPipeline pipeline =
        new InstrumentedReactiveEndpointGateEvaluationPipeline(delegate, registry);

    StepVerifier.create(pipeline.evaluate(createContext(GATE_ID)))
        .expectNextMatches(AccessDecision.Allowed.class::isInstance)
        .verifyComplete();

    assertThat(counterValue(registry, GATE_ID, "allowed")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordDeniedDisabledCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    ReactiveEndpointGateEvaluationPipeline delegate = createDenyingPipeline(DeniedReason.DISABLED);
    InstrumentedReactiveEndpointGateEvaluationPipeline pipeline =
        new InstrumentedReactiveEndpointGateEvaluationPipeline(delegate, registry);

    StepVerifier.create(pipeline.evaluate(createContext(GATE_ID)))
        .expectNextMatches(AccessDecision.Denied.class::isInstance)
        .verifyComplete();

    assertThat(counterValue(registry, GATE_ID, "denied.disabled")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordDeniedScheduleInactiveCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    ReactiveEndpointGateEvaluationPipeline delegate = createDenyingPipelineWithSchedule();
    InstrumentedReactiveEndpointGateEvaluationPipeline pipeline =
        new InstrumentedReactiveEndpointGateEvaluationPipeline(delegate, registry);

    StepVerifier.create(pipeline.evaluate(createContext(GATE_ID)))
        .expectNextMatches(AccessDecision.Denied.class::isInstance)
        .verifyComplete();

    assertThat(counterValue(registry, GATE_ID, "denied.schedule_inactive")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordDeniedConditionNotMetCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    ReactiveEndpointGateEvaluationPipeline delegate =
        createDenyingPipeline(DeniedReason.CONDITION_NOT_MET);
    InstrumentedReactiveEndpointGateEvaluationPipeline pipeline =
        new InstrumentedReactiveEndpointGateEvaluationPipeline(delegate, registry);

    EvaluationContext context = createContextWithCondition(GATE_ID);
    StepVerifier.create(pipeline.evaluate(context))
        .expectNextMatches(AccessDecision.Denied.class::isInstance)
        .verifyComplete();

    assertThat(counterValue(registry, GATE_ID, "denied.condition_not_met")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordDeniedRolloutExcludedCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    ReactiveEndpointGateEvaluationPipeline delegate =
        createDenyingPipeline(DeniedReason.ROLLOUT_EXCLUDED);
    InstrumentedReactiveEndpointGateEvaluationPipeline pipeline =
        new InstrumentedReactiveEndpointGateEvaluationPipeline(delegate, registry);

    EvaluationContext context = createContextWithRollout(GATE_ID);
    StepVerifier.create(pipeline.evaluate(context))
        .expectNextMatches(AccessDecision.Denied.class::isInstance)
        .verifyComplete();

    assertThat(counterValue(registry, GATE_ID, "denied.rollout_excluded")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordTimer() {
    MeterRegistry registry = new SimpleMeterRegistry();
    ReactiveEndpointGateEvaluationPipeline delegate = createAllowingPipeline();
    InstrumentedReactiveEndpointGateEvaluationPipeline pipeline =
        new InstrumentedReactiveEndpointGateEvaluationPipeline(delegate, registry);

    StepVerifier.create(pipeline.evaluate(createContext(GATE_ID)))
        .expectNextCount(1)
        .verifyComplete();

    assertThat(
            registry
                .find("endpoint.gate.evaluation.duration")
                .tag("gate.id", GATE_ID)
                .tag("outcome", "allowed")
                .timer())
        .isNotNull();
  }

  @Test
  void shouldIncrementCounterOnMultipleEvaluations() {
    MeterRegistry registry = new SimpleMeterRegistry();
    ReactiveEndpointGateEvaluationPipeline delegate = createAllowingPipeline();
    InstrumentedReactiveEndpointGateEvaluationPipeline pipeline =
        new InstrumentedReactiveEndpointGateEvaluationPipeline(delegate, registry);

    EvaluationContext context = createContext(GATE_ID);
    StepVerifier.create(pipeline.evaluate(context)).expectNextCount(1).verifyComplete();
    StepVerifier.create(pipeline.evaluate(context)).expectNextCount(1).verifyComplete();
    StepVerifier.create(pipeline.evaluate(context)).expectNextCount(1).verifyComplete();

    assertThat(counterValue(registry, GATE_ID, "allowed")).isEqualTo(3.0);
  }

  @Test
  void shouldRecordMetricsPerGateId() {
    MeterRegistry registry = new SimpleMeterRegistry();
    ReactiveEndpointGateEvaluationPipeline delegate = createAllowingPipeline();
    InstrumentedReactiveEndpointGateEvaluationPipeline pipeline =
        new InstrumentedReactiveEndpointGateEvaluationPipeline(delegate, registry);

    StepVerifier.create(pipeline.evaluate(createContext("gate-a")))
        .expectNextCount(1)
        .verifyComplete();
    StepVerifier.create(pipeline.evaluate(createContext("gate-b")))
        .expectNextCount(1)
        .verifyComplete();

    assertThat(counterValue(registry, "gate-a", "allowed")).isEqualTo(1.0);
    assertThat(counterValue(registry, "gate-b", "allowed")).isEqualTo(1.0);
  }

  private static double counterValue(MeterRegistry registry, String gateId, String outcome) {
    return registry
        .find("endpoint.gate.evaluations")
        .tag("gate.id", gateId)
        .tag("outcome", outcome)
        .counter()
        .count();
  }

  private static EvaluationContext createContext(String gateId) {
    return new EvaluationContext(gateId, "", 100, null, () -> null);
  }

  private static EvaluationContext createContextWithCondition(String gateId) {
    return new EvaluationContext(gateId, "some-condition", 100, null, () -> null);
  }

  private static EvaluationContext createContextWithRollout(String gateId) {
    return new EvaluationContext(gateId, "", 50, null, () -> new EndpointGateContext("test-user"));
  }

  private static ReactiveEndpointGateEvaluationPipeline createAllowingPipeline() {
    return new ReactiveEndpointGateEvaluationPipeline(
        new ReactiveEnabledEvaluationStep(gateId -> Mono.just(true)),
        new ReactiveScheduleEvaluationStep(gateId -> Mono.empty(), Clock.systemDefaultZone()),
        new ReactiveConditionEvaluationStep((expression, variables) -> Mono.just(true)),
        new ReactiveRolloutEvaluationStep((gateId, context, percentage) -> Mono.just(true)));
  }

  private static ReactiveEndpointGateEvaluationPipeline createDenyingPipeline(DeniedReason reason) {
    return new ReactiveEndpointGateEvaluationPipeline(
        new ReactiveEnabledEvaluationStep(gateId -> Mono.just(reason != DeniedReason.DISABLED)),
        new ReactiveScheduleEvaluationStep(gateId -> Mono.empty(), Clock.systemDefaultZone()),
        new ReactiveConditionEvaluationStep(
            (expression, variables) -> Mono.just(reason != DeniedReason.CONDITION_NOT_MET)),
        new ReactiveRolloutEvaluationStep(
            (gateId, context, percentage) -> Mono.just(reason != DeniedReason.ROLLOUT_EXCLUDED)));
  }

  private static ReactiveEndpointGateEvaluationPipeline createDenyingPipelineWithSchedule() {
    ZoneId zone = ZoneId.of("UTC");
    Instant retryAfter = Instant.parse("2026-01-01T00:00:00Z");
    LocalDateTime futureStart = LocalDateTime.ofInstant(retryAfter, zone);
    LocalDateTime futureEnd = futureStart.plusHours(1);
    Clock pastClock = Clock.fixed(retryAfter.minusSeconds(60), zone);
    return new ReactiveEndpointGateEvaluationPipeline(
        new ReactiveEnabledEvaluationStep(gateId -> Mono.just(true)),
        new ReactiveScheduleEvaluationStep(
            gateId -> Mono.just(new Schedule(futureStart, futureEnd, zone)), pastClock),
        new ReactiveConditionEvaluationStep((expression, variables) -> Mono.just(true)),
        new ReactiveRolloutEvaluationStep((gateId, context, percentage) -> Mono.just(true)));
  }
}
