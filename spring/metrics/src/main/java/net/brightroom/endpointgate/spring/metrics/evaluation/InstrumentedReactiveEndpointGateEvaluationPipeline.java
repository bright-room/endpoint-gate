package net.brightroom.endpointgate.spring.metrics.evaluation;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.brightroom.endpointgate.core.evaluation.AccessDecision;
import net.brightroom.endpointgate.core.evaluation.EvaluationContext;
import net.brightroom.endpointgate.reactive.core.evaluation.ReactiveEndpointGateEvaluationPipeline;
import reactor.core.publisher.Mono;

/**
 * Decorator that wraps a {@link ReactiveEndpointGateEvaluationPipeline} to record Micrometer
 * metrics for each gate evaluation.
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
public class InstrumentedReactiveEndpointGateEvaluationPipeline
    extends ReactiveEndpointGateEvaluationPipeline {

  private static final String METRIC_EVALUATIONS = "endpoint.gate.evaluations";
  private static final String METRIC_DURATION = "endpoint.gate.evaluation.duration";
  private static final String TAG_GATE_ID = "gate.id";
  private static final String TAG_OUTCOME = "outcome";

  private final ReactiveEndpointGateEvaluationPipeline delegate;
  private final MeterRegistry meterRegistry;

  /**
   * Creates a new instrumented reactive pipeline that delegates to the given pipeline and records
   * metrics to the given registry.
   *
   * @param delegate the underlying reactive pipeline to delegate evaluation to
   * @param meterRegistry the meter registry to record metrics to
   */
  public InstrumentedReactiveEndpointGateEvaluationPipeline(
      ReactiveEndpointGateEvaluationPipeline delegate, MeterRegistry meterRegistry) {
    super(
        NoOpReactiveEvaluationSteps.enabled(),
        NoOpReactiveEvaluationSteps.schedule(),
        NoOpReactiveEvaluationSteps.condition(),
        NoOpReactiveEvaluationSteps.rollout());
    this.delegate = delegate;
    this.meterRegistry = meterRegistry;
  }

  /**
   * Evaluates the gate via the delegate pipeline and records counter and timer metrics upon
   * completion.
   *
   * @param context the evaluation context
   * @return a {@link Mono} emitting the access decision from the delegate pipeline
   */
  @Override
  public Mono<AccessDecision> evaluate(EvaluationContext context) {
    Timer.Sample sample = Timer.start(meterRegistry);
    return delegate
        .evaluate(context)
        .doOnSuccess(
            decision -> {
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
            });
  }
}
