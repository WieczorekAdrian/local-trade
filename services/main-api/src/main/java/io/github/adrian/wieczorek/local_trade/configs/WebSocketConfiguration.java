package io.github.adrian.wieczorek.local_trade.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
  private final CookieHandshakeInterceptor cookieHandshakeInterceptor;

  @Value("${allowed.interceptor.origins}")
  private String allowedInterceptorOrigins;

  public WebSocketConfiguration(CookieHandshakeInterceptor cookieHandshakeInterceptor) {
    this.cookieHandshakeInterceptor = cookieHandshakeInterceptor;
  }

  @Override
  public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOrigins(allowedInterceptorOrigins)
        .addInterceptors(cookieHandshakeInterceptor)
        .setHandshakeHandler(new CustomHandshakeHandler());
  }
}
