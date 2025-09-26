package com.team.socialnetwork.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.team.socialnetwork.security.JwtService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final AuthChannelInterceptor authChannelInterceptor;

    public WebSocketConfig(JwtService jwtService, AuthChannelInterceptor authChannelInterceptor) {
        this.jwtService = jwtService;
        this.authChannelInterceptor = authChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para chat
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {

                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request,
                                                   ServerHttpResponse response,
                                                   WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) throws Exception {
                        // Extraer token de header Authorization
                        String authHeader = request.getHeaders().getFirst("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7);
                            String email = jwtService.extractSubject(token); // email o username según generaste token
                            attributes.put("principal", (Principal) () -> email);
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request,
                                               ServerHttpResponse response,
                                               WebSocketHandler wsHandler,
                                               Exception ex) {
                        // no hace nada
                    }
                })
                .withSockJS();
                
        // Endpoint para notificaciones - Con logs detallados para debugging
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {

                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request,
                                                   ServerHttpResponse response,
                                                   WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) throws Exception {
                        System.out.println("=== 🔌 WEBSOCKET HANDSHAKE INICIADO ===");
                        System.out.println("📍 Request URI: " + request.getURI());
                        System.out.println("📍 Request Method: " + request.getMethod());
                        System.out.println("📍 All Headers: " + request.getHeaders());
                        
                        try {
                            String token = null;
                            
                            // Opción 1: Extraer token de header Authorization
                            String authHeader = request.getHeaders().getFirst("Authorization");
                            System.out.println("🔍 Authorization Header: " + authHeader);
                            
                            if (authHeader != null) {
                                if (authHeader.startsWith("Bearer ")) {
                                    token = authHeader.substring(7);
                                } else {
                                    // Si no tiene Bearer, usar directamente el valor (para Postman)
                                    token = authHeader;
                                }
                                System.out.println("✅ Token encontrado en Authorization header");
                                System.out.println("🔑 Token preview: " + token.substring(0, Math.min(50, token.length())) + "...");
                            } 
                            // Opción 2: Extraer token de query parameter (backup para Postman)
                            else {
                                String queryString = request.getURI().getQuery();
                                System.out.println("🔍 Query String: " + queryString);
                                
                                if (queryString != null && queryString.contains("token=")) {
                                    String[] params = queryString.split("&");
                                    for (String param : params) {
                                        if (param.startsWith("token=")) {
                                            token = param.substring("token=".length());
                                            System.out.println("✅ Token encontrado en query parameter");
                                            System.out.println("🔑 Token preview: " + token.substring(0, Math.min(50, token.length())) + "...");
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            if (token != null) {
                                System.out.println("🔍 Validando token con JwtService...");
                                String email = jwtService.extractSubject(token);
                                System.out.println("📧 Email extraído del token: " + email);
                                
                                boolean isValid = jwtService.isTokenValid(token, email);
                                System.out.println("🔒 Token válido: " + isValid);
                                
                                if (isValid) {
                                    attributes.put("principal", (Principal) () -> email);
                                    System.out.println("✅ WebSocket handshake successful for user: " + email);
                                    System.out.println("=== 🔌 HANDSHAKE COMPLETADO EXITOSAMENTE ===\n");
                                    return true;
                                } else {
                                    System.out.println("❌ Invalid JWT token in WebSocket handshake");
                                    System.out.println("=== 🔌 HANDSHAKE FALLÓ - TOKEN INVÁLIDO ===\n");
                                    return false;
                                }
                            } else {
                                System.out.println("❌ No token found in Authorization header or query parameter");
                                System.out.println("💡 Para Postman, usa:");
                                System.out.println("   - Header: Authorization: tu_jwt_token");
                                System.out.println("   - O Query: /ws?token=tu_jwt_token");
                                System.out.println("=== 🔌 HANDSHAKE FALLÓ - SIN TOKEN ===\n");
                                return false;
                            }
                        } catch (Exception e) {
                            System.out.println("❌ Error in WebSocket handshake: " + e.getMessage());
                            System.out.println("📍 Stack trace:");
                            e.printStackTrace();
                            System.out.println("=== 🔌 HANDSHAKE FALLÓ - EXCEPCIÓN ===\n");
                            return false;
                        }
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request,
                                               ServerHttpResponse response,
                                               WebSocketHandler wsHandler,
                                               Exception ex) {
                        System.out.println("=== 🔌 WEBSOCKET AFTER HANDSHAKE ===");
                        if (ex != null) {
                            System.out.println("❌ WebSocket handshake failed: " + ex.getMessage());
                            ex.printStackTrace();
                        } else {
                            System.out.println("✅ WebSocket handshake completed successfully");
                        }
                        System.out.println("=== 🔌 AFTER HANDSHAKE FINALIZADO ===\n");
                    }
                })
                .withSockJS();
        
        // Endpoint ADICIONAL para WebSocket nativo (sin SockJS) - Para frontend directo
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {

                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request,
                                                   ServerHttpResponse response,
                                                   WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) throws Exception {
                        System.out.println("=== 🔌 WEBSOCKET NATIVO HANDSHAKE INICIADO ===");
                        System.out.println("📍 Request URI: " + request.getURI());
                        System.out.println("📍 All Headers: " + request.getHeaders());
                        
                        try {
                            String token = null;
                            
                            // Extraer token de header Authorization
                            String authHeader = request.getHeaders().getFirst("Authorization");
                            System.out.println("🔍 Authorization Header: " + authHeader);
                            
                            if (authHeader != null) {
                                if (authHeader.startsWith("Bearer ")) {
                                    token = authHeader.substring(7);
                                } else {
                                    token = authHeader;
                                }
                                System.out.println("✅ Token encontrado");
                            }
                            // También buscar en query parameter
                            else {
                                String queryString = request.getURI().getQuery();
                                if (queryString != null && queryString.contains("token=")) {
                                    String[] params = queryString.split("&");
                                    for (String param : params) {
                                        if (param.startsWith("token=")) {
                                            token = param.substring("token=".length());
                                            System.out.println("✅ Token encontrado en query");
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            if (token != null) {
                                String email = jwtService.extractSubject(token);
                                boolean isValid = jwtService.isTokenValid(token, email);
                                System.out.println("🔒 Token válido: " + isValid + " para: " + email);
                                
                                if (isValid) {
                                    attributes.put("principal", (Principal) () -> email);
                                    System.out.println("✅ WebSocket NATIVO handshake exitoso");
                                    return true;
                                }
                            }
                            
                            System.out.println("❌ WebSocket NATIVO handshake falló");
                            return false;
                        } catch (Exception e) {
                            System.out.println("❌ Error en WebSocket NATIVO: " + e.getMessage());
                            e.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request,
                                               ServerHttpResponse response,
                                               WebSocketHandler wsHandler,
                                               Exception ex) {
                        System.out.println("=== 🔌 AFTER HANDSHAKE NATIVO ===");
                        if (ex != null) {
                            System.out.println("❌ Error: " + ex.getMessage());
                        } else {
                            System.out.println("✅ Handshake NATIVO completado");
                        }
                    }
                });
        // Sin .withSockJS() para WebSocket nativo
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
