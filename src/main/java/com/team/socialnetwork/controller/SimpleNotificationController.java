package com.team.socialnetwork.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.NotificationRepository;
import com.team.socialnetwork.repository.UserRepository;

@RestController
@RequestMapping("/api/notifications")
public class SimpleNotificationController {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public SimpleNotificationController(UserRepository userRepository, 
                                       NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> getSimpleNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Not authenticated");
                return ResponseEntity.status(401).body(error);
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(404).body(error);
            }

            // Obtener todas las notificaciones sin paginación por ahora
            List<com.team.socialnetwork.entity.Notification> notifications = 
                notificationRepository.findAll().stream()
                    .filter(n -> n.getRecipient().getId().equals(user.getId()))
                    .limit(size)
                    .collect(Collectors.toList());

            // Crear respuesta simple
            List<Map<String, Object>> simpleNotifications = notifications.stream()
                .map(n -> {
                    Map<String, Object> simple = new HashMap<>();
                    simple.put("id", n.getId());
                    simple.put("type", n.getType().toString());
                    simple.put("isRead", n.isRead());
                    simple.put("createdAt", n.getCreatedAt().toString());
                    simple.put("actorId", n.getActor().getId());
                    simple.put("actorUsername", n.getActor().getUsername());
                    return simple;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", simpleNotifications);
            response.put("total", simpleNotifications.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal error: " + e.getMessage());
            error.put("details", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }
}