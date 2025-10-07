package com.team.socialnetwork.entity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Notification Entity Tests")
class NotificationTest {

    @Test
    @DisplayName("Should create Notification with default constructor")
    void shouldCreateNotificationWithDefaultConstructor() {
        Notification notification = new Notification();

        assertNull(notification.getId());
        assertNull(notification.getRecipient());
        assertNull(notification.getActor());
        assertNull(notification.getType());
        assertNull(notification.getPost());
        assertNull(notification.getComment());
        assertFalse(notification.isRead());
        assertNull(notification.getCreatedAt());
    }

    @Test
    @DisplayName("Should create Notification with basic constructor")
    void shouldCreateNotificationWithBasicConstructor() {
        User recipient = new User();
        recipient.setId(1L);
        recipient.setUsername("recipient");

        User actor = new User();
        actor.setId(2L);
        actor.setUsername("actor");

        Notification.NotificationType type = Notification.NotificationType.FOLLOW;

        Notification notification = new Notification(recipient, actor, type);

        assertEquals(recipient, notification.getRecipient());
        assertEquals(actor, notification.getActor());
        assertEquals(type, notification.getType());
        assertNull(notification.getPost());
        assertNull(notification.getComment());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("Should create Notification with post constructor")
    void shouldCreateNotificationWithPostConstructor() {
        User recipient = new User();
        recipient.setId(1L);

        User actor = new User();
        actor.setId(2L);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Test post");

        Notification.NotificationType type = Notification.NotificationType.LIKE;

        Notification notification = new Notification(recipient, actor, type, post);

        assertEquals(recipient, notification.getRecipient());
        assertEquals(actor, notification.getActor());
        assertEquals(type, notification.getType());
        assertEquals(post, notification.getPost());
        assertNull(notification.getComment());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("Should create Notification with full constructor")
    void shouldCreateNotificationWithFullConstructor() {
        User recipient = new User();
        recipient.setId(1L);

        User actor = new User();
        actor.setId(2L);

        Post post = new Post();
        post.setId(1L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");

        Notification.NotificationType type = Notification.NotificationType.COMMENT_LIKE;

        Notification notification = new Notification(recipient, actor, type, post, comment);

        assertEquals(recipient, notification.getRecipient());
        assertEquals(actor, notification.getActor());
        assertEquals(type, notification.getType());
        assertEquals(post, notification.getPost());
        assertEquals(comment, notification.getComment());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        Notification notification = new Notification();
        Long id = 1L;
        Instant createdAt = Instant.now();
        
        User recipient = new User();
        recipient.setUsername("recipient");
        
        User actor = new User();
        actor.setUsername("actor");
        
        Post post = new Post();
        post.setDescription("Test post");
        
        Comment comment = new Comment();
        comment.setText("Test comment");

        notification.setId(id);
        notification.setCreatedAt(createdAt);
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(Notification.NotificationType.COMMENT);
        notification.setPost(post);
        notification.setComment(comment);
        notification.setRead(true);

        assertEquals(id, notification.getId());
        assertEquals(createdAt, notification.getCreatedAt());
        assertEquals(recipient, notification.getRecipient());
        assertEquals(actor, notification.getActor());
        assertEquals(Notification.NotificationType.COMMENT, notification.getType());
        assertEquals(post, notification.getPost());
        assertEquals(comment, notification.getComment());
        assertTrue(notification.isRead());
    }

    @Test
    @DisplayName("Should handle isRead flag correctly")
    void shouldHandleIsReadFlagCorrectly() {
        Notification notification = new Notification();

        // Default should be false
        assertFalse(notification.isRead());

        // Set to true
        notification.setRead(true);
        assertTrue(notification.isRead());

        // Set back to false
        notification.setRead(false);
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("Should handle all notification types")
    void shouldHandleAllNotificationTypes() {
        Notification notification = new Notification();

        // Test all enum values
        notification.setType(Notification.NotificationType.LIKE);
        assertEquals(Notification.NotificationType.LIKE, notification.getType());

        notification.setType(Notification.NotificationType.POST_LIKE);
        assertEquals(Notification.NotificationType.POST_LIKE, notification.getType());

        notification.setType(Notification.NotificationType.COMMENT);
        assertEquals(Notification.NotificationType.COMMENT, notification.getType());

        notification.setType(Notification.NotificationType.COMMENT_LIKE);
        assertEquals(Notification.NotificationType.COMMENT_LIKE, notification.getType());

        notification.setType(Notification.NotificationType.FOLLOW);
        assertEquals(Notification.NotificationType.FOLLOW, notification.getType());

        notification.setType(Notification.NotificationType.FOLLOW_REQUEST);
        assertEquals(Notification.NotificationType.FOLLOW_REQUEST, notification.getType());
    }
}