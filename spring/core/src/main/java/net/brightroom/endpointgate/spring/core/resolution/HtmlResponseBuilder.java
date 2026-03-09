package net.brightroom.endpointgate.spring.core.resolution;

import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.core.exception.EndpointGateScheduleInactiveException;
import org.springframework.web.util.HtmlUtils;

/** Utility for building HTML responses for endpoint gate access denial. */
public final class HtmlResponseBuilder {

  private HtmlResponseBuilder() {}

  /**
   * Builds an HTML body for a denied endpoint gate access.
   *
   * <p>If the exception is a {@link EndpointGateScheduleInactiveException}, the response title
   * indicates temporary unavailability (503). Otherwise, a 403 access denied title is used.
   *
   * @param e the exception that triggered the denial
   * @return an HTML string
   */
  public static String buildHtml(EndpointGateAccessDeniedException e) {
    String statusText;
    if (e instanceof EndpointGateScheduleInactiveException) {
      statusText = "503 - Service Temporarily Unavailable";
    } else {
      statusText = "403 - Access Denied";
    }
    String escapedMessage = HtmlUtils.htmlEscape(e.getMessage());
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Access Denied</title>
        </head>
        <body>
          <h1>%s</h1>
          <p>%s</p>
        </body>
        </html>
        """
        .formatted(statusText, escapedMessage);
  }
}
