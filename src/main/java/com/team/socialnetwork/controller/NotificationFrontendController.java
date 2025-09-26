package com.team.socialnetwork.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.socialnetwork.dto.MessageResponse;
import com.team.socialnetwork.entity.Notification;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.NotificationRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.service.NotificationService;

/**
 * Controlador de notificaciones para compatibilidad con el frontend
 * Usando ruta /notifications (sin /api) para compatibilidad directa
 */
@RestController
@RequestMapping("/notifications")
public class NotificationFrontendController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationFrontendController(NotificationService notificationService,
                                        UserRepository userRepository,
                                        NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Obtener notificaciones del usuario - Compatible con frontend
     * Devuelve formato esperado por el frontend React
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        System.out.println("📋 Frontend solicitando notificaciones - Usuario: " + authentication.getName());
        
        User user = getUserFromAuth(authentication);
        
        try {
            // Limitar tamaño máximo
            if (size > 50) size = 50;

            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notificationsPage = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);

            List<Map<String, Object>> notificationsList = new ArrayList<>();
            
            for (Notification notification : notificationsPage.getContent()) {
                try {
                    Map<String, Object> notifMap = new HashMap<>();
                    notifMap.put("id", notification.getId());
                    notifMap.put("type", notification.getType().toString());
                    notifMap.put("read", notification.isRead());
                    notifMap.put("createdAt", notification.getCreatedAt().toString());
                    
                    // Actor info con validación para evitar "NaNd"
                    User actor = notification.getActor();
                    if (actor != null) {
                        String actorName = actor.getFullName();
                        String actorUsername = actor.getUsername();
                        
                        // Validar y limpiar valores null/vacíos
                        if (actorName == null || actorName.trim().isEmpty() || "null".equals(actorName)) {
                            actorName = actorUsername != null && !actorUsername.trim().isEmpty() 
                                ? actorUsername : "Usuario " + actor.getId();
                        }
                        
                        if (actorUsername == null || actorUsername.trim().isEmpty() || "null".equals(actorUsername)) {
                            actorUsername = "user_" + actor.getId();
                        }
                        
                        notifMap.put("actorId", actor.getId());
                        notifMap.put("actorName", actorName.trim());
                        notifMap.put("actorUsername", actorUsername.trim());
                    } else {
                        // Datos por defecto si actor es null
                        notifMap.put("actorId", -1);
                        notifMap.put("actorName", "Usuario desconocido");
                        notifMap.put("actorUsername", "unknown_user");
                    }
                    
                    // Generar mensaje descriptivo
                    String message = generateNotificationMessage(notification);
                    notifMap.put("message", message);
                    
                    // Post info si existe
                    if (notification.getPost() != null) {
                        notifMap.put("postId", notification.getPost().getId());
                    }
                    
                    // Comment info si existe
                    if (notification.getComment() != null) {
                        notifMap.put("commentId", notification.getComment().getId());
                    }
                    
                    notificationsList.add(notifMap);
                    
                } catch (Exception e) {
                    System.err.println("❌ Error procesando notificación " + notification.getId() + ": " + e.getMessage());
                    // Continuar con la siguiente notificación en lugar de fallar completamente
                }
            }

            // Contar no leídas
            long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationsList);
            response.put("unreadCount", unreadCount);
            response.put("total", notificationsPage.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", notificationsPage.getTotalPages());

            System.out.println("✅ Enviadas " + notificationsList.size() + " notificaciones, " + unreadCount + " sin leer");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo notificaciones: " + e.getMessage());
            e.printStackTrace();
            
            // Respuesta de fallback
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("notifications", new ArrayList<>());
            errorResponse.put("unreadCount", 0);
            errorResponse.put("error", "Error loading notifications: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Marcar notificación como leída
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<MessageResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId) {

        System.out.println("📖 Marcando notificación " + notificationId + " como leída");
        
        User user = getUserFromAuth(authentication);
        boolean success = notificationService.markAsRead(user.getId(), notificationId);

        if (success) {
            System.out.println("✅ Notificación " + notificationId + " marcada como leída");
            return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
        } else {
            System.err.println("❌ No se pudo marcar notificación " + notificationId + " como leída");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener contador de no leídas
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
        
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Generar mensaje descriptivo para la notificación
     */
    private String generateNotificationMessage(Notification notification) {
        String actorName = "Alguien";
        
        if (notification.getActor() != null) {
            String fullName = notification.getActor().getFullName();
            String username = notification.getActor().getUsername();
            
            if (fullName != null && !fullName.trim().isEmpty() && !"null".equals(fullName)) {
                actorName = fullName.trim();
            } else if (username != null && !username.trim().isEmpty() && !"null".equals(username)) {
                actorName = username.trim();
            } else {
                actorName = "Usuario " + notification.getActor().getId();
            }
        }

        switch (notification.getType()) {
            case FOLLOW:
                return actorName + " comenzó a seguirte";
            case FOLLOW_REQUEST:
                return actorName + " te envió una solicitud de amistad";
            case LIKE:
            case POST_LIKE:
                return actorName + " le gustó tu post";
            case COMMENT:
                return actorName + " comentó tu post";
            case COMMENT_LIKE:
                return actorName + " le gustó tu comentario";
            default:
                return actorName + " realizó una acción";
        }
    }

    /**
     * Obtener usuario autenticado
     */
    private User getUserFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
}