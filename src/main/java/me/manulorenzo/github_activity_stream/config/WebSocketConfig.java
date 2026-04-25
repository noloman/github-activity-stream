package me.manulorenzo.github_activity_stream.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic"); // Enables a simple in-memory broker for topics
    registry.setApplicationDestinationPrefixes("/app"); // Prefix for app-specific destinations
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws") // Endpoint for WebSocket connections
        .setAllowedOriginPatterns(
            "*") // Use allowedOriginPatterns instead of setAllowedOrigins with "*"
        .withSockJS(); // Enable SockJS fallback options
  }
}
