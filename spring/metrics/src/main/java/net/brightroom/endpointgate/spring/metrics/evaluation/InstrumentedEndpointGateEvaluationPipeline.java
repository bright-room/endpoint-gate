package net.brightroom.endpointgate.spring.metrics.evaluation;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EndpointGateEvaluationPipeline;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;

/**
 * Decorator that wraps an {@link EndpointGateEvaluationPipeline} to record Micrometer metrics for
 * each gate evaluation.
 *
 * <p>Records the following metrics per evaluation:
 *
 * <ul>
 *   <li>{@code endpoint.gate.evaluations} (Counter) — tagged with {@code gate.id} and {@code
 *       outcome}
 *   <li>{@code endpoint.gate.evaluation.duration} (Timer) — tagged with {@code gate.id} and {@code
 *       outcome}
 * </ul>
 */
public class InstrumentedEndpointGateEvaluationPipeline extends EndpointGateEvaluationPipeline {

  private static final String METRIC_EVALUATIONS = "endpoint.gate.evaluations";
  private static final String METRIC_DURATION = "endpoint.gate.evaluation.duration";
  private static final String TAG_GATE_ID = "gate.id";
  private static final String TAG_OUTCOME = "outcome";

  private final EndpointGateEvaluationPipeline delegate;
  private final MeterRegistry meterRegistry;

  /**
   * Creates a new instrumented pipeline that delegates to the given pipeline and records metrics to
   * the given registry.
   *
   * @param delegate the underlying pipeline to delegate evaluation to
   * @param meterRegistry the meter registry to record metrics to
   */
  public InstrumentedEndpointGateEvaluationPipeline(
      EndpointGateEvaluationPipeline delegate, MeterRegistry meterRegistry) {
    super(
        NoOpEvaluationSteps.enabled(),
        NoOpEvaluationSteps.schedule(),
        NoOpEvaluationSteps.condition(),
        NoOpEvaluationSteps.rollout());
    this.delegate = delegate;
    this.meterRegistry = meterRegistry;
  }

  /**
   * Evaluates the gate via the delegate pipeline and records counter and timer metrics.
   *
   * @param context the evaluation context
   * @return the access decision from the delegate pipeline
   */
  @Override
  public AccessDecision evaluate(EvaluationContext context) {
    Timer.Sample sample = Timer.start(meterRegistry);
    AccessDecision decision = delegate.evaluate(context);

    String outcome = OutcomeResolver.resolve(decision);
    String gateId = context.gateId();

    Counter.builder(METRIC_EVALUATIONS)
        .tag(TAG_GATE_ID, gateId)
        .tag(TAG_OUTCOME, outcome)
        .register(meterRegistry)
        .increment();

    sample.stop(
        Timer.builder(METRIC_DURATION)
            .tag(TAG_GATE_ID, gateId)
            .tag(TAG_OUTCOME, outcome)
            .register(meterRegistry));

    return decision;
  }
}
