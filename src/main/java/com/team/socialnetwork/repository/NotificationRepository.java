package com.team.socialnetwork.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.socialnetwork.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Obtener notificaciones paginadas de un usuario, ordenadas por fecha de creación (más recientes primero)
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    /**
     * Contar notificaciones no leídas de un usuario
     */
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    /**
     * Marcar todas las notificaciones de un usuario como leídas
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * Marcar una notificación específica como leída si pertenece al usuario
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.recipient.id = :recipientId")
    int markAsReadByIdAndRecipientId(@Param("notificationId") Long notificationId, @Param("recipientId") Long recipientId);

    /**
     * Buscar notificación específica para evitar duplicados
     * (útil para evitar múltiples notificaciones del mismo tipo del mismo actor)
     */
    Optional<Notification> findByRecipientIdAndActorIdAndTypeAndPostId(
            Long recipientId, Long actorId, Notification.NotificationType type, Long postId);

    Optional<Notification> findByRecipientIdAndActorIdAndTypeAndCommentId(
            Long recipientId, Long actorId, Notification.NotificationType type, Long commentId);

    Optional<Notification> findByRecipientIdAndActorIdAndType(
            Long recipientId, Long actorId, Notification.NotificationType type);

    /**
     * Eliminar notificación específica (para cuando se hace unlike, unfollow, etc.)
     */
    void deleteByRecipientIdAndActorIdAndTypeAndPostId(
            Long recipientId, Long actorId, Notification.NotificationType type, Long postId);

    void deleteByRecipientIdAndActorIdAndTypeAndCommentId(
            Long recipientId, Long actorId, Notification.NotificationType type, Long commentId);

    void deleteByRecipientIdAndActorIdAndType(
            Long recipientId, Long actorId, Notification.NotificationType type);
}