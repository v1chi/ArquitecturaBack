package com.team.socialnetwork.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.socialnetwork.dto.NotificationCountResponse;
import com.team.socialnetwork.dto.NotificationResponse;
import com.team.socialnetwork.entity.Comment;
import com.team.socialnetwork.entity.Notification;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.NotificationRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.entity.Notification;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.NotificationRepository;
import com.team.socialnetwork.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, 
                              SimpMessagingTemplate messagingTemplate,
                              UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    /**
     * Crear y enviar notificación por WebSocket
     */
    @Transactional
    public void createAndSendNotification(User recipient, User actor, Notification.NotificationType type) {
        createAndSendNotification(recipient, actor, type, null, null);
    }

    @Transactional
    public void createAndSendNotification(User recipient, User actor, Notification.NotificationType type, Post post) {
        createAndSendNotification(recipient, actor, type, post, null);
    }

    @Transactional
    public void createAndSendNotification(User recipient, User actor, Notification.NotificationType type, 
                                        Post post, Comment comment) {
        // Evitar auto-notificaciones
        if (recipient.getId().equals(actor.getId())) {
            return;
        }

        // Verificar si ya existe esta notificación para evitar spam
        if (shouldSkipNotification(recipient.getId(), actor.getId(), type, 
                                  post != null ? post.getId() : null, 
                                  comment != null ? comment.getId() : null)) {
            return;
        }

        // Crear la notificación
        Notification notification = new Notification(recipient, actor, type, post, comment);
        notificationRepository.save(notification);

        // Convertir a DTO y enviar por WebSocket
        NotificationResponse notificationResponse = convertToResponse(notification);
        sendNotificationByWebSocket(recipient.getId(), notificationResponse);

        // También enviar actualización del contador
        sendUnreadCountUpdate(recipient.getId());
    }

    /**
     * Eliminar notificación cuando se deshace una acción (unlike, unfollow)
     */
    @Transactional
    public void removeNotification(User recipient, User actor, Notification.NotificationType type) {
        notificationRepository.deleteByRecipientIdAndActorIdAndType(
            recipient.getId(), actor.getId(), type);
        sendUnreadCountUpdate(recipient.getId());
    }

    @Transactional
    public void removeNotification(User recipient, User actor, Notification.NotificationType type, Post post) {
        notificationRepository.deleteByRecipientIdAndActorIdAndTypeAndPostId(
            recipient.getId(), actor.getId(), type, post.getId());
        sendUnreadCountUpdate(recipient.getId());
    }

    @Transactional
    public void removeNotification(User recipient, User actor, Notification.NotificationType type, Comment comment) {
        notificationRepository.deleteByRecipientIdAndActorIdAndTypeAndCommentId(
            recipient.getId(), actor.getId(), type, comment.getId());
        sendUnreadCountUpdate(recipient.getId());
    }

    /**
     * Obtener notificaciones paginadas del usuario
     */
    public Page<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToResponse);
    }

    /**
     * Obtener contador de notificaciones no leídas
     */
    public NotificationCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(userId);
        return new NotificationCountResponse(count);
    }

    /**
     * Marcar todas las notificaciones como leídas
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsReadByRecipientId(userId);
        if (updated > 0) {
            sendUnreadCountUpdate(userId);
        }
        return updated;
    }

    /**
     * Marcar una notificación específica como leída
     */
    @Transactional
    public boolean markAsRead(Long userId, Long notificationId) {
        int updated = notificationRepository.markAsReadByIdAndRecipientId(notificationId, userId);
        if (updated > 0) {
            sendUnreadCountUpdate(userId);
            return true;
        }
        return false;
    }

    private boolean shouldSkipNotification(Long recipientId, Long actorId, Notification.NotificationType type, 
                                         Long postId, Long commentId) {
        // Verificar si ya existe la notificación exacta para evitar duplicados
        if (postId != null && commentId != null) {
            return notificationRepository.findByRecipientIdAndActorIdAndTypeAndCommentId(
                recipientId, actorId, type, commentId).isPresent();
        } else if (postId != null) {
            return notificationRepository.findByRecipientIdAndActorIdAndTypeAndPostId(
                recipientId, actorId, type, postId).isPresent();
        } else {
            return notificationRepository.findByRecipientIdAndActorIdAndType(
                recipientId, actorId, type).isPresent();
        }
    }

    private void sendNotificationByWebSocket(Long userId, NotificationResponse notification) {
        try {
            String destination = "/topic/notifications/" + userId;
            NotificationWebSocketMessage message = new NotificationWebSocketMessage("NEW_NOTIFICATION", notification, null);
            
            System.out.println("📡 Enviando notificación WebSocket:");
            System.out.println("   🎯 Destino: " + destination);
            System.out.println("   📄 Tipo: " + notification.getType());
            System.out.println("   👤 Actor: " + (notification.getActor() != null ? notification.getActor().getFullName() : "null"));
            
            messagingTemplate.convertAndSend(destination, message);
            System.out.println("✅ Notificación WebSocket enviada exitosamente");
        } catch (Exception e) {
            System.err.println("❌ Error enviando notificación WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendUnreadCountUpdate(Long userId) {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(userId);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, 
                new NotificationWebSocketMessage("UNREAD_COUNT_UPDATE", null, count));
    }

    private NotificationResponse convertToResponse(Notification notification) {
        try {
            // Verificar que el actor no sea null y tenga datos válidos
            User actor = notification.getActor();
            if (actor == null) {
                System.err.println("❌ Actor es null en notificación " + notification.getId());
                return null; // O manejar de otra forma
            }

            // Obtener username y fullName con valores por defecto
            String username = actor.getUsername();
            String fullName = actor.getFullName();
            
            // Validar que no sean null o strings vacíos
            if (username == null || username.trim().isEmpty()) {
                username = "user_" + actor.getId();
                System.err.println("⚠️ Username null/vacío para usuario " + actor.getId() + ", usando: " + username);
            }
            
            if (fullName == null || fullName.trim().isEmpty()) {
                fullName = "Usuario " + actor.getId();
                System.err.println("⚠️ FullName null/vacío para usuario " + actor.getId() + ", usando: " + fullName);
            }

            NotificationResponse.ActorInfo actorInfo = new NotificationResponse.ActorInfo(
                actor.getId(),
                username.trim(),
                fullName.trim()
            );

            NotificationResponse.PostInfo post = null;
            if (notification.getPost() != null) {
                String description = notification.getPost().getDescription();
                post = new NotificationResponse.PostInfo(
                    notification.getPost().getId(),
                    truncateText(description != null ? description : "Sin descripción", 50)
                );
            }

            NotificationResponse.CommentInfo comment = null;
            if (notification.getComment() != null) {
                String text = notification.getComment().getText();
                comment = new NotificationResponse.CommentInfo(
                    notification.getComment().getId(),
                    truncateText(text != null ? text : "Sin comentario", 50)
                );
            }

            return new NotificationResponse(
                notification.getId(),
                notification.getType().toString(),
                actorInfo,
                post,
                comment,
                notification.isRead(),
                notification.getCreatedAt()
            );
        } catch (Exception e) {
            System.err.println("❌ Error convirtiendo notificación " + notification.getId() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Devolver una respuesta básica segura en caso de error
            NotificationResponse.ActorInfo fallbackActor = new NotificationResponse.ActorInfo(
                notification.getActor() != null ? notification.getActor().getId() : -1L,
                "unknown_user",
                "Usuario desconocido"
            );
            
            return new NotificationResponse(
                notification.getId(),
                notification.getType().toString(),
                fallbackActor,
                null,
                null,
                notification.isRead(),
                notification.getCreatedAt()
            );
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * Actualiza una notificación existente de FOLLOW_REQUEST y la reenvía por WebSocket
     */
    public void updateAndResendFollowRequestNotification(Long recipientId, Long actorId) {
        try {
            System.out.println("🔔 Actualizando notificación FOLLOW_REQUEST existente:");
            System.out.println("   👤 Recipient ID: " + recipientId);
            System.out.println("   👥 Actor ID: " + actorId);
            
            // Buscar la notificación FOLLOW_REQUEST existente
            Optional<Notification> existingNotificationOpt = notificationRepository
                .findByRecipientIdAndActorIdAndType(
                    recipientId, 
                    actorId, 
                    Notification.NotificationType.FOLLOW_REQUEST
                );
            
            if (!existingNotificationOpt.isPresent()) {
                System.out.println("   ⚠️ No se encontró notificación FOLLOW_REQUEST existente, creando nueva...");
                // Si no existe, crear nueva (esto no debería pasar, pero por seguridad)
                User recipient = userRepository.findById(recipientId).orElse(null);
                User actor = userRepository.findById(actorId).orElse(null);
                if (recipient != null && actor != null) {
                    createAndSendNotification(recipient, actor, Notification.NotificationType.FOLLOW_REQUEST, null, null);
                }
                return;
            }
            
            // Tomar la notificación encontrada
            Notification notification = existingNotificationOpt.get();
            
            // Actualizar timestamp y marcar como no leída
            notification.setCreatedAt(java.time.Instant.now());
            notification.setRead(false);
            
            // Guardar la notificación actualizada
            notificationRepository.save(notification);
            
            System.out.println("   ✅ Notificación actualizada - Nueva fecha: " + notification.getCreatedAt());
            System.out.println("   📤 Reenviando por WebSocket...");
            
            // Convertir a DTO
            NotificationResponse notificationResponse = convertToResponse(notification);
            
            // Reenviar por WebSocket
            sendNotificationByWebSocket(recipientId, notificationResponse);
            
            // Enviar actualización del contador
            sendUnreadCountUpdate(recipientId);
            
            System.out.println("   ✅ Notificación FOLLOW_REQUEST actualizada y reenviada exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error actualizando notificación FOLLOW_REQUEST: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Clase interna para mensajes WebSocket
    public static class NotificationWebSocketMessage {
        private final String type;
        private final NotificationResponse notification;
        private final Long unreadCount;

        public NotificationWebSocketMessage(String type, NotificationResponse notification, Long unreadCount) {
            this.type = type;
            this.notification = notification;
            this.unreadCount = unreadCount;
        }

        // Getters
        public String getType() { return type; }
        public NotificationResponse getNotification() { return notification; }
        public Long getUnreadCount() { return unreadCount; }
    }
}