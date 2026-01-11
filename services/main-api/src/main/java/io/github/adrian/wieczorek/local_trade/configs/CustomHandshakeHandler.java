package io.github.adrian.wieczorek.local_trade.configs;

import lombok.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

  @Override
  protected Principal determineUser(@NonNull ServerHttpRequest request,
      @NonNull WebSocketHandler wsHandler, Map<String, Object> attributes) {
    if (attributes.get("userEmail") instanceof String email) {
      return new UsernamePasswordAuthenticationToken(email, null, null);
    }
    return null;
  }
}
