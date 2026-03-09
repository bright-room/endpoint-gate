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
   * <p>If the exception is a {@link EndpointGateScheduleInactiveException}, both the page title and
   * heading indicate temporary unavailability (503). Otherwise, a 403 access denied title and
   * heading are used.
   *
   * @param e the exception that triggered the denial
   * @return an HTML string
   */
  public static String buildHtml(EndpointGateAccessDeniedException e) {
    String pageTitle = resolvePageTitle(e);
    String statusText = resolveStatusText(e);
    String escapedMessage = HtmlUtils.htmlEscape(e.getMessage());
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>%s</title>
        </head>
        <body>
          <h1>%s</h1>
          <p>%s</p>
        </body>
        </html>
        """
        .formatted(pageTitle, statusText, escapedMessage);
  }

  private static String resolvePageTitle(EndpointGateAccessDeniedException e) {
    if (e instanceof EndpointGateScheduleInactiveException) {
      return "Service Temporarily Unavailable";
    }
    return "Access Denied";
  }

  private static String resolveStatusText(EndpointGateAccessDeniedException e) {
    if (e instanceof EndpointGateScheduleInactiveException) {
      return "503 - Service Temporarily Unavailable";
    }
    return "403 - Access Denied";
  }
}
