package net.brightroom.endpointgate.core.validation;

import java.util.HashSet;
import java.util.Set;

/**
 * Validates gate ID arrays to prevent fail-open behavior caused by null, empty, or blank gate IDs.
 *
 * <p>This validator is shared across annotation-based and programmatic gate enforcement to ensure
 * consistent validation behavior.
 */
public final class GateIdValidator {

  private GateIdValidator() {}

  /**
   * Validates that the given gate ID array is non-null, non-empty, contains no null or blank
   * elements, and contains no duplicate entries.
   *
   * @param gateIds the array of gate identifiers to validate
   * @throws IllegalArgumentException if {@code gateIds} is null, empty, contains null/blank
   *     elements, or contains duplicate gate IDs
   */
  public static void validateGateIds(String[] gateIds) {
    if (gateIds == null) {
      throw new IllegalArgumentException(
          "gateIds must not be null or empty. An empty value causes fail-open behavior and allows access unconditionally.");
    }
    if (gateIds.length == 0) {
      throw new IllegalArgumentException(
          "gateIds must not be null or empty. An empty value causes fail-open behavior and allows access unconditionally.");
    }
    for (String gateId : gateIds) {
      if (gateId == null) {
        throw new IllegalArgumentException(
            "gateId must not be null or blank. A blank value causes fail-open behavior and allows access unconditionally.");
      }
      if (gateId.isBlank()) {
        throw new IllegalArgumentException(
            "gateId must not be null or blank. A blank value causes fail-open behavior and allows access unconditionally.");
      }
    }
    Set<String> seen = new HashSet<>();
    for (String gateId : gateIds) {
      if (!seen.add(gateId)) {
        throw new IllegalArgumentException(
            String.format(
                "gateIds must not contain duplicates, but '%s' appears more than once.", gateId));
      }
    }
  }
}
