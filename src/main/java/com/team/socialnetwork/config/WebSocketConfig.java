package com.team.socialnetwork.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para conectarse via SockJS (cliente usa /chat)
        registry.addEndpoint("/chat").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para enviar mensajes al controlador del backend
        registry.setApplicationDestinationPrefixes("/app");
        // Prefijo simple broker para reenvío a clientes
        registry.enableSimpleBroker("/topic");
    }
}
