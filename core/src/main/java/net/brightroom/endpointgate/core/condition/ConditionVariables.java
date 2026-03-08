package net.brightroom.endpointgate.core.condition;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Immutable holder for request context variables available in condition expressions.
 *
 * <p>Instances are created via {@link ConditionVariablesBuilder}.
 *
 * <p>Available properties:
 *
 * <ul>
 *   <li>{@code headers} — {@code Map<String, String>} of request headers (first value per name,
 *       case-insensitive keys)
 *   <li>{@code params} — {@code Map<String, String>} of query parameters (first value per name)
 *   <li>{@code cookies} — {@code Map<String, String>} of cookie values keyed by cookie name
 *   <li>{@code path} — {@code String} request path (e.g. {@code /api/resource})
 *   <li>{@code method} — {@code String} HTTP method (e.g. {@code GET}, {@code POST})
 *   <li>{@code remoteAddress} — {@code String} client IP address
 * </ul>
 */
public final class ConditionVariables {

  private final Map<String, String> headers;
  private final Map<String, String> params;
  private final Map<String, String> cookies;
  private final String path;
  private final String method;
  private final String remoteAddress;

  ConditionVariables(
      Map<String, String> headers,
      Map<String, String> params,
      Map<String, String> cookies,
      String path,
      String method,
      String remoteAddress) {
    TreeMap<String, String> headersCopy = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    headersCopy.putAll(headers);
    this.headers = Collections.unmodifiableMap(headersCopy);
    this.params = Map.copyOf(params);
    this.cookies = Map.copyOf(cookies);
    this.path = path;
    this.method = method;
    this.remoteAddress = remoteAddress;
  }

  /**
   * Returns the request headers as a case-insensitive map.
   *
   * @return unmodifiable map of header names to values
   */
  public Map<String, String> headers() {
    return headers;
  }

  /**
   * Returns the query parameters.
   *
   * @return unmodifiable map of parameter names to values
   */
  public Map<String, String> params() {
    return params;
  }

  /**
   * Returns the cookies.
   *
   * @return unmodifiable map of cookie names to values
   */
  public Map<String, String> cookies() {
    return cookies;
  }

  /**
   * Returns the request path.
   *
   * @return the request path
   */
  public String path() {
    return path;
  }

  /**
   * Returns the HTTP method.
   *
   * @return the HTTP method
   */
  public String method() {
    return method;
  }

  /**
   * Returns the client IP address.
   *
   * @return the client IP address
   */
  public String remoteAddress() {
    return remoteAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConditionVariables that)) return false;
    return Objects.equals(headers, that.headers)
        && Objects.equals(params, that.params)
        && Objects.equals(cookies, that.cookies)
        && Objects.equals(path, that.path)
        && Objects.equals(method, that.method)
        && Objects.equals(remoteAddress, that.remoteAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(headers, params, cookies, path, method, remoteAddress);
  }

  @Override
  public String toString() {
    return "ConditionVariables["
        + "headers="
        + headers
        + ", params="
        + params
        + ", cookies="
        + cookies
        + ", path="
        + path
        + ", method="
        + method
        + ", remoteAddress="
        + remoteAddress
        + ']';
  }
}
