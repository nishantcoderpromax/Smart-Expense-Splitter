package com.splitter.config;

import com.splitter.security.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
 
/**
 * Clients only ever SUBSCRIBE here — there are no client-to-server @MessageMapping
 * endpoints, because every mutation still goes through the normal REST API (so
 * validation, auth checks, and business logic stay in one place). The backend
 * pushes a small "something changed" event to /topic/groups/{id} whenever a
 * REST call successfully changes that group's data; the frontend just refetches
 * over REST when it hears one, rather than trusting socket-delivered data directly.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
 
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
 
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("http://localhost:5173", "http://localhost:4173") // dev server + preview server
                .withSockJS();
    }
 
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }
}