package net.brightroom.endpointgate.core.condition;

import java.util.Map;

/**
 * Builder for constructing {@link ConditionVariables} passed to {@link
 * EndpointGateConditionEvaluator}.
 *
 * <p>Centralizes the variable key names so that all modules ({@code webmvc}, {@code webflux}) use
 * consistent keys. Each module extracts the request-specific data and delegates to this builder to
 * construct the {@link ConditionVariables}.
 *
 * <p>Available variable keys:
 *
 * <ul>
 *   <li>{@code headers} — {@code Map<String, String>} of request headers (first value per name)
 *   <li>{@code params} — {@code Map<String, String>} of query parameters (first value per name)
 *   <li>{@code cookies} — {@code Map<String, String>} of cookie values keyed by cookie name
 *   <li>{@code path} — {@code String} request path (e.g. {@code /api/resource})
 *   <li>{@code method} — {@code String} HTTP method (e.g. {@code GET}, {@code POST})
 *   <li>{@code remoteAddress} — {@code String} client IP address
 * </ul>
 */
public final class ConditionVariablesBuilder {

  /** Creates a new builder with default (empty) values. */
  public ConditionVariablesBuilder() {}

  private Map<String, String> headers = Map.of();
  private Map<String, String> params = Map.of();
  private Map<String, String> cookies = Map.of();
  private String path = "";
  private String method = "";
  private String remoteAddress = "";

  /**
   * Sets the {@code headers} variable.
   *
   * @param headers the request headers
   * @return this builder
   */
  public ConditionVariablesBuilder headers(Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  /**
   * Sets the {@code params} variable.
   *
   * @param params the query parameters
   * @return this builder
   */
  public ConditionVariablesBuilder params(Map<String, String> params) {
    this.params = params;
    return this;
  }

  /**
   * Sets the {@code cookies} variable.
   *
   * @param cookies the cookies
   * @return this builder
   */
  public ConditionVariablesBuilder cookies(Map<String, String> cookies) {
    this.cookies = cookies;
    return this;
  }

  /**
   * Sets the {@code path} variable.
   *
   * @param path the request path
   * @return this builder
   */
  public ConditionVariablesBuilder path(String path) {
    this.path = path;
    return this;
  }

  /**
   * Sets the {@code method} variable.
   *
   * @param method the HTTP method
   * @return this builder
   */
  public ConditionVariablesBuilder method(String method) {
    this.method = method;
    return this;
  }

  /**
   * Sets the {@code remoteAddress} variable.
   *
   * @param remoteAddress the client IP address
   * @return this builder
   */
  public ConditionVariablesBuilder remoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
    return this;
  }

  /**
   * Returns the built {@link ConditionVariables}.
   *
   * @return a new {@code ConditionVariables} instance
   */
  public ConditionVariables build() {
    return new ConditionVariables(headers, params, cookies, path, method, remoteAddress);
  }
}
