package com.team.socialnetwork.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.socialnetwork.dto.MessageResponse;
import com.team.socialnetwork.dto.NotificationCountResponse;
import com.team.socialnetwork.dto.NotificationResponse;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final com.team.socialnetwork.repository.NotificationRepository notificationRepository;

    public NotificationController(NotificationService notificationService, 
                                 UserRepository userRepository,
                                 com.team.socialnetwork.repository.NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Obtener notificaciones paginadas del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        User user = getUserFromAuth(authentication);
        
        // Validar tamaño de página
        if (size > 50) {
            size = 50; // Limitar el tamaño máximo para rendimiento
        }

        try {
            Page<NotificationResponse> notifications = notificationService.getUserNotifications(
                    user.getId(), page, size);

            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo notificaciones: " + e.getMessage());
            e.printStackTrace();
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving notifications");
        }
    }

    /**
     * Endpoint de prueba para obtener notificaciones como entidades simples
     */
    @GetMapping("/raw")
    public ResponseEntity<java.util.Map<String, Object>> getNotificationsRaw(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        User user = getUserFromAuth(authentication);
        
        try {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
            org.springframework.data.domain.Page<com.team.socialnetwork.entity.Notification> notifications = 
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);
            
            java.util.List<java.util.Map<String, Object>> simplifiedNotifications = new java.util.ArrayList<>();
            
            for (com.team.socialnetwork.entity.Notification notification : notifications.getContent()) {
                java.util.Map<String, Object> simple = new java.util.HashMap<>();
                simple.put("id", notification.getId());
                simple.put("type", notification.getType().toString());
                simple.put("isRead", notification.isRead());
                simple.put("createdAt", notification.getCreatedAt());
                simple.put("actorId", notification.getActor().getId());
                simple.put("actorUsername", notification.getActor().getUsername());
                
                if (notification.getPost() != null) {
                    simple.put("postId", notification.getPost().getId());
                }
                if (notification.getComment() != null) {
                    simple.put("commentId", notification.getComment().getId());
                }
                
                simplifiedNotifications.add(simple);
            }
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("content", simplifiedNotifications);
            response.put("totalElements", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("number", notifications.getNumber());
            response.put("size", notifications.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo notificaciones raw: " + e.getMessage());
            e.printStackTrace();
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    /**
     * Obtener contador de notificaciones no leídas
     */
    @GetMapping("/unread-count")
    public ResponseEntity<NotificationCountResponse> getUnreadCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        User user = getUserFromAuth(authentication);
        NotificationCountResponse count = notificationService.getUnreadCount(user.getId());

        return ResponseEntity.ok(count);
    }

    /**
     * Endpoint adicional para compatibilidad con frontend (devuelve solo el número)
     */
    @GetMapping("/count")
    public ResponseEntity<java.util.Map<String, Long>> getSimpleUnreadCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        User user = getUserFromAuth(authentication);
        NotificationCountResponse count = notificationService.getUnreadCount(user.getId());

        java.util.Map<String, Long> response = new java.util.HashMap<>();
        response.put("count", count.getUnreadCount());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Marcar todas las notificaciones como leídas
     */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<MessageResponse> markAllAsRead(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        User user = getUserFromAuth(authentication);
        int updated = notificationService.markAllAsRead(user.getId());

        return ResponseEntity.ok(new MessageResponse(updated + " notifications marked as read"));
    }

    /**
     * Marcar una notificación específica como leída
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<MessageResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        User user = getUserFromAuth(authentication);
        boolean success = notificationService.markAsRead(user.getId(), notificationId);

        if (!success) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Notification not found");
        }

        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    // Método auxiliar para obtener usuario autenticado
    private User getUserFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
    }
}