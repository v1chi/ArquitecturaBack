package com.team.socialnetwork.config;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
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
            System.out.println("=== 📨 STOMP MESSAGE INTERCEPTED ===");
            System.out.println("📍 Command: " + accessor.getCommand());
            System.out.println("📍 Destination: " + accessor.getDestination());
            System.out.println("📍 Session: " + accessor.getSessionId());
            System.out.println("📍 Native Headers: " + accessor.toNativeHeaderMap());
            
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                System.out.println("🔄 Processing STOMP CONNECT command...");
                
                // Extraer token del header Authorization
                String authToken = accessor.getFirstNativeHeader("Authorization");
                System.out.println("🔍 Authorization header in STOMP: " + (authToken != null ? "[PRESENT]" : "[NOT FOUND]"));
                
                if (authToken != null && authToken.startsWith("Bearer ")) {
                    String token = authToken.substring(7);
                    System.out.println("🔑 Token found in STOMP header");
                    
                    try {
                        String email = jwtService.extractSubject(token);
                        System.out.println("📧 Email from STOMP token: " + email);
                        
                        if (jwtService.isTokenValid(token, email)) {
                            Principal principal = () -> email;
                            accessor.setUser(principal);
                            System.out.println("✅ WebSocket user authenticated via STOMP: " + email);
                        } else {
                            System.out.println("❌ Invalid JWT token in STOMP CONNECT");
                            System.out.println("=== 📨 STOMP CONNECT RECHAZADO ===\n");
                            return null; // Rechazar conexión
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Error validating JWT token in STOMP: " + e.getMessage());
                        e.printStackTrace();
                        System.out.println("=== 📨 STOMP CONNECT ERROR ===\n");
                        return null; // Rechazar conexión
                    }
                } else {
                    System.out.println("❌ No Authorization header in STOMP CONNECT");
                    System.out.println("=== 📨 STOMP CONNECT SIN AUTH ===\n");
                    return null; // Rechazar conexión
                }
                
                System.out.println("=== 📨 STOMP CONNECT PROCESADO ===\n");
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                System.out.println("📡 STOMP SUBSCRIBE to: " + accessor.getDestination());
                System.out.println("👤 User: " + (accessor.getUser() != null ? accessor.getUser().getName() : "ANONYMOUS"));
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                System.out.println("👋 STOMP DISCONNECT from: " + (accessor.getUser() != null ? accessor.getUser().getName() : "ANONYMOUS"));
            }
            
            System.out.println("=== 📨 STOMP MESSAGE PROCESSING COMPLETED ===\n");
        }
        
        return message;
    }
}