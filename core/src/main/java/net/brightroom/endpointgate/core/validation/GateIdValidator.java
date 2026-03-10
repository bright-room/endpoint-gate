package net.brightroom.endpointgate.core.validation;

/**
 * Validates gate ID arrays to prevent fail-open behavior caused by null, empty, or blank gate IDs.
 *
 * <p>This validator is shared across annotation-based and programmatic gate enforcement to ensure
 * consistent validation behavior.
 */
public final class GateIdValidator {

  private GateIdValidator() {}

  /**
   * Validates that the given gate ID array is non-null, non-empty, and contains no null or blank
   * elements.
   *
   * @param gateIds the array of gate identifiers to validate
   * @throws IllegalArgumentException if {@code gateIds} is null, empty, or contains null/blank
   *     elements
   */
  public static void validateGateIds(String[] gateIds) {
    if (gateIds == null) {
      throw new IllegalArgumentException(
          "gateIds must not be null or empty. "
              + "An empty value causes fail-open behavior and allows access unconditionally.");
    }
    if (gateIds.length == 0) {
      throw new IllegalArgumentException(
          "gateIds must not be null or empty. "
              + "An empty value causes fail-open behavior and allows access unconditionally.");
    }
    for (String gateId : gateIds) {
      if (gateId == null) {
        throw new IllegalArgumentException(
            "gateId must not be null or blank. "
                + "A blank value causes fail-open behavior and allows access unconditionally.");
      }
      if (gateId.isBlank()) {
        throw new IllegalArgumentException(
            "gateId must not be null or blank. "
                + "A blank value causes fail-open behavior and allows access unconditionally.");
      }
    }
  }
}
