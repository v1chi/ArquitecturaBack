package com.team.socialnetwork.service;

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

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, 
                              SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
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
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, 
                new NotificationWebSocketMessage("NEW_NOTIFICATION", notification, null));
    }

    private void sendUnreadCountUpdate(Long userId) {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(userId);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, 
                new NotificationWebSocketMessage("UNREAD_COUNT_UPDATE", null, count));
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse.ActorInfo actor = new NotificationResponse.ActorInfo(
            notification.getActor().getId(),
            notification.getActor().getUsername(),
            notification.getActor().getFullName()
        );

        NotificationResponse.PostInfo post = null;
        if (notification.getPost() != null) {
            post = new NotificationResponse.PostInfo(
                notification.getPost().getId(),
                truncateText(notification.getPost().getDescription(), 50)
            );
        }

        NotificationResponse.CommentInfo comment = null;
        if (notification.getComment() != null) {
            comment = new NotificationResponse.CommentInfo(
                notification.getComment().getId(),
                truncateText(notification.getComment().getText(), 50)
            );
        }

        return new NotificationResponse(
            notification.getId(),
            notification.getType().toString(),
            actor,
            post,
            comment,
            notification.isRead(),
            notification.getCreatedAt()
        );
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
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