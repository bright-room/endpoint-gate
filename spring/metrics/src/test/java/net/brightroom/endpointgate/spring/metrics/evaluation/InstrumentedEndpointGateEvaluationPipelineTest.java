package net.brightroom.endpointgate.spring.metrics.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.Optional;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.AccessDecision.DeniedReason;
import net.brightroom.endpointgate.core.evaluation.ConditionEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.EnabledEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.core.evaluation.RolloutEvaluationStep;
import net.brightroom.endpointgate.core.evaluation.ScheduleEvaluationStep;
import org.junit.jupiter.api.Test;

class InstrumentedEndpointGateEvaluationPipelineTest {

  private static final String GATE_ID = "test-gate";

  @Test
  void shouldRecordAllowedCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    EndpointGateEvaluationPipeline delegate = createAllowingPipeline();
    InstrumentedEndpointGateEvaluationPipeline pipeline =
        new InstrumentedEndpointGateEvaluationPipeline(delegate, registry);

    AccessDecision decision = pipeline.evaluate(createContext(GATE_ID));

    assertThat(decision).isInstanceOf(AccessDecision.Allowed.class);
    assertThat(counterValue(registry, GATE_ID, "allowed")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordDeniedDisabledCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    EndpointGateEvaluationPipeline delegate = createDenyingPipeline(DeniedReason.DISABLED);
    InstrumentedEndpointGateEvaluationPipeline pipeline =
        new InstrumentedEndpointGateEvaluationPipeline(delegate, registry);

    AccessDecision decision = pipeline.evaluate(createContext(GATE_ID));

    assertThat(decision).isInstanceOf(AccessDecision.Denied.class);
    assertThat(counterValue(registry, GATE_ID, "denied.disabled")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordDeniedScheduleInactiveCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    EndpointGateEvaluationPipeline delegate =
        createDenyingPipelineWithRetryAfter(
            DeniedReason.SCHEDULE_INACTIVE, Instant.parse("2026-01-01T00:00:00Z"));
    InstrumentedEndpointGateEvaluationPipeline pipeline =
        new InstrumentedEndpointGateEvaluationPipeline(delegate, registry);

    AccessDecision decision = pipeline.evaluate(createContext(GATE_ID));

    assertThat(decision).isInstanceOf(AccessDecision.Denied.class);
    assertThat(counterValue(registry, GATE_ID, "denied.schedule_inactive")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordDeniedConditionNotMetCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    EndpointGateEvaluationPipeline delegate = createDenyingPipeline(DeniedReason.CONDITION_NOT_MET);
    InstrumentedEndpointGateEvaluationPipeline pipeline =
        new InstrumentedEndpointGateEvaluationPipeline(delegate, registry);

    EvaluationContext context = createContextWithCondition(GATE_ID);
    AccessDecision decision = pipeline.evaluate(context);

    assertThat(decision).isInstanceOf(AccessDecision.Denied.class);
    assertThat(counterValue(registry, GATE_ID, "denied.condition_not_met")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordDeniedRolloutExcludedCounter() {
    MeterRegistry registry = new SimpleMeterRegistry();
    EndpointGateEvaluationPipeline delegate = createDenyingPipeline(DeniedReason.ROLLOUT_EXCLUDED);
    InstrumentedEndpointGateEvaluationPipeline pipeline =
        new InstrumentedEndpointGateEvaluationPipeline(delegate, registry);

    EvaluationContext context = createContextWithRollout(GATE_ID);
    AccessDecision decision = pipeline.evaluate(context);

    assertThat(decision).isInstanceOf(AccessDecision.Denied.class);
    assertThat(counterValue(registry, GATE_ID, "denied.rollout_excluded")).isEqualTo(1.0);
  }

  @Test
  void shouldRecordTimer() {
    MeterRegistry registry = new SimpleMeterRegistry();
    EndpointGateEvaluationPipeline delegate = createAllowingPipeline();
    InstrumentedEndpointGateEvaluationPipeline pipeline =
        new InstrumentedEndpointGateEvaluationPipeline(delegate, registry);

    pipeline.evaluate(createContext(GATE_ID));

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
    EndpointGateEvaluationPipeline delegate = createAllowingPipeline();
    InstrumentedEndpointGateEvaluationPipeline pipeline =
        new InstrumentedEndpointGateEvaluationPipeline(delegate, registry);

    pipeline.evaluate(createContext(GATE_ID));
    pipeline.evaluate(createContext(GATE_ID));
    pipeline.evaluate(createContext(GATE_ID));

    assertThat(counterValue(registry, GATE_ID, "allowed")).isEqualTo(3.0);
  }

  @Test
  void shouldRecordMetricsPerGateId() {
    MeterRegistry registry = new SimpleMeterRegistry();
    EndpointGateEvaluationPipeline delegate = createAllowingPipeline();
    InstrumentedEndpointGateEvaluationPipeline pipeline =
        new InstrumentedEndpointGateEvaluationPipeline(delegate, registry);

    pipeline.evaluate(createContext("gate-a"));
    pipeline.evaluate(createContext("gate-b"));

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
    return new EvaluationContext(
        gateId,
        "",
        50,
        null,
        () -> new net.brightroom.endpointgate.core.context.EndpointGateContext("test-user"));
  }

  private static EndpointGateEvaluationPipeline createAllowingPipeline() {
    return new EndpointGateEvaluationPipeline(
        new EnabledEvaluationStep(gateId -> true),
        new ScheduleEvaluationStep(gateId -> Optional.empty(), java.time.Clock.systemDefaultZone()),
        new ConditionEvaluationStep((expression, variables) -> true),
        new RolloutEvaluationStep((gateId, context, percentage) -> true));
  }

  private static EndpointGateEvaluationPipeline createDenyingPipeline(DeniedReason reason) {
    return new EndpointGateEvaluationPipeline(
        new EnabledEvaluationStep(gateId -> reason != DeniedReason.DISABLED),
        new ScheduleEvaluationStep(gateId -> Optional.empty(), java.time.Clock.systemDefaultZone()),
        new ConditionEvaluationStep(
            (expression, variables) -> reason != DeniedReason.CONDITION_NOT_MET),
        new RolloutEvaluationStep(
            (gateId, context, percentage) -> reason != DeniedReason.ROLLOUT_EXCLUDED));
  }

  private static EndpointGateEvaluationPipeline createDenyingPipelineWithRetryAfter(
      DeniedReason reason, Instant retryAfter) {
    if (reason != DeniedReason.SCHEDULE_INACTIVE) {
      return createDenyingPipeline(reason);
    }
    java.time.ZoneId zone = java.time.ZoneId.of("UTC");
    java.time.LocalDateTime futureStart = java.time.LocalDateTime.ofInstant(retryAfter, zone);
    java.time.LocalDateTime futureEnd = futureStart.plusHours(1);
    java.time.Clock pastClock = java.time.Clock.fixed(retryAfter.minusSeconds(60), zone);
    return new EndpointGateEvaluationPipeline(
        new EnabledEvaluationStep(gateId -> true),
        new ScheduleEvaluationStep(
            gateId ->
                Optional.of(
                    new net.brightroom.endpointgate.core.provider.Schedule(
                        futureStart, futureEnd, zone)),
            pastClock),
        new ConditionEvaluationStep((expression, variables) -> true),
        new RolloutEvaluationStep((gateId, context, percentage) -> true));
  }
}
