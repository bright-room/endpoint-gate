package net.brightroom.endpointgate.spring.webmvc.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import net.brightroom.endpointgate.core.context.EndpointGateContext;
import org.junit.jupiter.api.Test;

class RandomEndpointGateContextResolverTest {

  private final RandomEndpointGateContextResolver resolver =
      new RandomEndpointGateContextResolver();
  private final HttpServletRequest request = mock(HttpServletRequest.class);

  @Test
  void resolve_returnsNonEmptyContext() {
    Optional<EndpointGateContext> result = resolver.resolve(request);
    assertThat(result).isPresent();
  }

  @Test
  void resolve_returnsContextWithNonBlankIdentifier() {
    Optional<EndpointGateContext> result = resolver.resolve(request);
    assertThat(result).isPresent();
    assertThat(result.get().userIdentifier()).isNotBlank();
  }

  @Test
  void resolve_returnsDifferentContextPerRequest() {
    Optional<EndpointGateContext> first = resolver.resolve(request);
    Optional<EndpointGateContext> second = resolver.resolve(request);
    assertThat(first).isPresent();
    assertThat(second).isPresent();
    assertThat(first.get().userIdentifier()).isNotEqualTo(second.get().userIdentifier());
  }
}
