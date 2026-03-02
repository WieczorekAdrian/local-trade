package io.github.adrian.wieczorek.local_trade.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final CookieHandshakeInterceptor cookieHandshakeInterceptor;

    @Value("${allowed.websocket.origins}")
    private String allowedWebsocketOrigins;

    public WebSocketConfiguration(CookieHandshakeInterceptor cookieHandshakeInterceptor) {
        this.cookieHandshakeInterceptor = cookieHandshakeInterceptor;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(new ThreadPoolTaskScheduler())
                .setHeartbeatValue(new long[]{10000, 10000});

        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedWebsocketOrigins.split(","))
                .addInterceptors(cookieHandshakeInterceptor)
                .setHandshakeHandler(new CustomHandshakeHandler());
    }
}