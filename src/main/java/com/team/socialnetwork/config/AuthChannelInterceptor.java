package com.team.socialnetwork.config;

import java.security.Principal;
import java.util.Collections;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.team.socialnetwork.security.JwtService;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public AuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // Solo procesamos STOMP CONNECT
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                // Extraer token del header "Authorization"
                String authToken = accessor.getFirstNativeHeader("Authorization");
                if (authToken != null && authToken.startsWith("Bearer ")) {
                    String token = authToken.substring(7); // quitar "Bearer "
                    try {
                        String email = jwtService.extractSubject(token);

                        if (jwtService.isTokenValid(token, email)) {
                            // Crear Authentication completo y asignarlo
                            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                            accessor.setUser(auth); // ahora Principal principal tendrá valor
                            System.out.println("✅ WebSocket user authenticated via STOMP: " + email);
                        } else {
                            System.out.println("❌ Invalid JWT token in STOMP CONNECT");
                            throw new IllegalArgumentException("Invalid JWT token");
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Error validating JWT token in STOMP: " + e.getMessage());
                        throw new IllegalArgumentException("Error validating JWT token", e);
                    }
                } else {
                    System.out.println("❌ No Authorization header in STOMP CONNECT");
                    throw new IllegalArgumentException("Missing JWT token");
                }
            }

            // Opcional: logs para SUBSCRIBE y DISCONNECT
            else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                System.out.println("📡 STOMP SUBSCRIBE to: " + accessor.getDestination() +
                        " | User: " + (accessor.getUser() != null ? accessor.getUser().getName() : "ANONYMOUS"));
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                System.out.println("👋 STOMP DISCONNECT | User: " + (accessor.getUser() != null ? accessor.getUser().getName() : "ANONYMOUS"));
            }
        }

        return message;
    }
}
