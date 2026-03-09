package net.brightroom.endpointgate.core.exception;

/**
 * Thrown when access to a gate-protected endpoint is denied.
 *
 * <p>This exception can be caught by a {@code @ControllerAdvice} to customize the response. If not
 * handled, the library's default response behavior applies.
 */
public class EndpointGateAccessDeniedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** The identifier of the gate that is not available. */
  private final String gateId;

  /**
   * Constructor.
   *
   * @param gateId the identifier of the gate that is not available
   */
  public EndpointGateAccessDeniedException(String gateId) {
    super(String.format("Gate '%s' is not available", gateId));
    this.gateId = gateId;
  }

  /**
   * Constructor for subclasses that need to customize the exception message.
   *
   * @param gateId the identifier of the gate that is not available
   * @param message the detail message
   */
  protected EndpointGateAccessDeniedException(String gateId, String message) {
    super(message);
    this.gateId = gateId;
  }

  /**
   * Returns the identifier of the gate that is not available.
   *
   * @return the identifier of the gate
   */
  public String gateId() {
    return gateId;
  }
}
