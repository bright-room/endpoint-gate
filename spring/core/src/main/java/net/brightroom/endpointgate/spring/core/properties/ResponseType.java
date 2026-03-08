package net.brightroom.endpointgate.spring.core.properties;

/**
 * Represents the type of response to be returned by the system.
 *
 * <ul>
 *   <li>PLAIN_TEXT: Denotes a plain text response.
 *   <li>JSON: Denotes a JSON-formatted response.
 *   <li>HTML: Denotes a simple fixed HTML response.
 * </ul>
 */
public enum ResponseType {
  /** Plain text response. */
  PLAIN_TEXT,

  /** JSON response. */
  JSON,

  /** HTML response. */
  HTML
}
