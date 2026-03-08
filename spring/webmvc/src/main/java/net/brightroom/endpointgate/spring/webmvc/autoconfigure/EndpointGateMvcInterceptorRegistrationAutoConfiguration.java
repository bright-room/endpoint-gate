package net.brightroom.endpointgate.spring.webmvc.autoconfigure;

import net.brightroom.endpointgate.spring.webmvc.interceptor.EndpointGateInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration that registers {@link
 * net.brightroom.endpointgate.spring.webmvc.interceptor.EndpointGateInterceptor} with the Spring
 * MVC interceptor registry for all request paths ({@code /**}).
 *
 * <p>This configuration runs after {@link EndpointGateMvcAutoConfiguration} to ensure the
 * interceptor bean is available before registration.
 */
@AutoConfiguration(after = EndpointGateMvcAutoConfiguration.class)
public class EndpointGateMvcInterceptorRegistrationAutoConfiguration {

  /** Creates a new {@link EndpointGateMvcInterceptorRegistrationAutoConfiguration}. */
  EndpointGateMvcInterceptorRegistrationAutoConfiguration() {}

  @Configuration(proxyBeanMethods = false)
  static class EndpointGateMvcInterceptorRegistrationConfiguration implements WebMvcConfigurer {

    private final EndpointGateInterceptor endpointGateInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(endpointGateInterceptor).addPathPatterns("/**");
    }

    EndpointGateMvcInterceptorRegistrationConfiguration(
        EndpointGateInterceptor endpointGateInterceptor) {
      this.endpointGateInterceptor = endpointGateInterceptor;
    }
  }
}
