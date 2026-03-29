package net.brightroom.endpointgate.spring.metrics.evaluation;

import net.brightroom.endpointgate.core.evaluation.AccessDecision;

/**
 * Resolves the outcome tag value from an {@link AccessDecision}.
 *
 * <p>Maps each decision type to a string suitable for use as a Micrometer metric tag:
 *
 * <ul>
 *   <li>{@link AccessDecision.Allowed} → {@code "allowed"}
 *   <li>{@link AccessDecision.Denied} with {@code DISABLED} → {@code "denied.disabled"}
 *   <li>{@link AccessDecision.Denied} with {@code SCHEDULE_INACTIVE} → {@code
 *       "denied.schedule_inactive"}
 *   <li>{@link AccessDecision.Denied} with {@code CONDITION_NOT_MET} → {@code
 *       "denied.condition_not_met"}
 *   <li>{@link AccessDecision.Denied} with {@code ROLLOUT_EXCLUDED} → {@code
 *       "denied.rollout_excluded"}
 * </ul>
 */
final class OutcomeResolver {

  private static final String OUTCOME_ALLOWED = "allowed";

  private OutcomeResolver() {}

  /**
   * Resolves the outcome string from the given access decision.
   *
   * @param decision the access decision
   * @return the outcome tag value
   */
  static String resolve(AccessDecision decision) {
    if (decision instanceof AccessDecision.Allowed) {
      return OUTCOME_ALLOWED;
    }
    AccessDecision.Denied denied = (AccessDecision.Denied) decision;
    return switch (denied.reason()) {
      case DISABLED -> "denied.disabled";
      case SCHEDULE_INACTIVE -> "denied.schedule_inactive";
      case CONDITION_NOT_MET -> "denied.condition_not_met";
      case ROLLOUT_EXCLUDED -> "denied.rollout_excluded";
    };
  }
}
