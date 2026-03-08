package net.brightroom.example.eventlistener;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class EndpointGateCache {

  private final Map<String, Object> cache = new ConcurrentHashMap<>();

  public void put(String key, Object value) {
    cache.put(key, value);
  }

  public Optional<Object> get(String key) {
    return Optional.ofNullable(cache.get(key));
  }

  public void invalidate(String key) {
    cache.remove(key);
  }

  public Map<String, Object> getAll() {
    return Map.copyOf(cache);
  }
}
