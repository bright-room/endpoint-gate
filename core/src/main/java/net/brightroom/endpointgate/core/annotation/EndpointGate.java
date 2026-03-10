package net.brightroom.endpointgate.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Endpoint gate annotation to control access to specific endpoints. This annotation can be applied
 * at both method and class levels to manage endpoint availability.
 *
 * <p>Condition expressions and rollout percentages are configured via {@code
 * endpoint-gate.gates.<name>} in {@code application.yaml} rather than on the annotation itself.
 *
 * <p>Usage examples:
 *
 * <pre>{@code
 * // Method level (single gate)
 * {@literal @}EndpointGate("new-api")
 * public void newFeature() {
 *     // This method will only be accessible if "new-api" gate is enabled
 * }
 *
 * // Multiple gates (AND semantics)
 * {@literal @}EndpointGate({"feature-new-dashboard", "beta-users-only"})
 * public void restrictedFeature() {
 *     // Accessible only when both gates are enabled
 * }
 *
 * // Class level
 * {@literal @}EndpointGate("beta-features")
 * public class BetaController {
 *     // All methods in this class will only be accessible if "beta-features" is enabled
 * }
 * }</pre>
 *
 * <p>To configure a condition or rollout percentage, use {@code application.yaml}:
 *
 * <pre>{@code
 * endpoint-gate:
 *   gates:
 *     new-api:
 *       enabled: true
 *       condition: "headers['X-Beta'] != null"
 *       rollout: 50
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EndpointGate {

  /**
   * Specifies the gates associated with a method or class. The values represent the unique
   * identifiers of the gates that determine whether the annotated method or class is accessible.
   * All specified gates must permit access (AND semantics); if any gate denies access, the request
   * is rejected.
   *
   * <p>This element is required. {@code @EndpointGate} without a value will result in a
   * compile-time error. An explicit empty string (e.g., {@code @EndpointGate("")}) or an empty
   * array (e.g., {@code @EndpointGate({})}) is also not permitted and will cause an {@link
   * IllegalStateException} to be thrown at request time by the interceptor.
   *
   * @return the identifiers of the gates; must contain at least one non-empty string
   */
  String[] value();
}
