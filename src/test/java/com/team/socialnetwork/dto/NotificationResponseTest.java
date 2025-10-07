package com.team.socialnetwork.dto;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationResponse DTO Tests")
class NotificationResponseTest {

    @Test
    @DisplayName("Should create NotificationResponse with default constructor")
    void shouldCreateNotificationResponseWithDefaultConstructor() {
        NotificationResponse response = new NotificationResponse();

        assertNull(response.getId());
        assertNull(response.getType());
        assertNull(response.getActor());
        assertNull(response.getPost());
        assertNull(response.getComment());
        assertFalse(response.isRead());
        assertNull(response.getCreatedAt());
    }

    @Test
    @DisplayName("Should create NotificationResponse with parameterized constructor")
    void shouldCreateNotificationResponseWithParameterizedConstructor() {
        Long id = 1L;
        String type = "FOLLOW";
        NotificationResponse.ActorInfo actor = new NotificationResponse.ActorInfo(2L, "testuser", "Test User");
        NotificationResponse.PostInfo post = new NotificationResponse.PostInfo(3L, "Test post");
        NotificationResponse.CommentInfo comment = new NotificationResponse.CommentInfo(4L, "Test comment");
        boolean isRead = true;
        Instant createdAt = Instant.now();

        NotificationResponse response = new NotificationResponse(id, type, actor, post, comment, isRead, createdAt);

        assertEquals(id, response.getId());
        assertEquals(type, response.getType());
        assertEquals(actor, response.getActor());
        assertEquals(post, response.getPost());
        assertEquals(comment, response.getComment());
        assertTrue(response.isRead());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        NotificationResponse response = new NotificationResponse();
        Long id = 1L;
        String type = "LIKE";
        NotificationResponse.ActorInfo actor = new NotificationResponse.ActorInfo(2L, "user", "User Name");
        NotificationResponse.PostInfo post = new NotificationResponse.PostInfo(3L, "Post content");
        NotificationResponse.CommentInfo comment = new NotificationResponse.CommentInfo(4L, "Comment content");
        boolean isRead = false;
        Instant createdAt = Instant.now();

        response.setId(id);
        response.setType(type);
        response.setActor(actor);
        response.setPost(post);
        response.setComment(comment);
        response.setRead(isRead);
        response.setCreatedAt(createdAt);

        assertEquals(id, response.getId());
        assertEquals(type, response.getType());
        assertEquals(actor, response.getActor());
        assertEquals(post, response.getPost());
        assertEquals(comment, response.getComment());
        assertEquals(isRead, response.isRead());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("Should create ActorInfo correctly")
    void shouldCreateActorInfoCorrectly() {
        Long id = 1L;
        String username = "testuser";
        String fullName = "Test User";

        NotificationResponse.ActorInfo actorInfo = new NotificationResponse.ActorInfo(id, username, fullName);

        assertEquals(id, actorInfo.getId());
        assertEquals(username, actorInfo.getUsername());
        assertEquals(fullName, actorInfo.getFullName());
    }

    @Test
    @DisplayName("Should set and get ActorInfo properties")
    void shouldSetAndGetActorInfoProperties() {
        NotificationResponse.ActorInfo actorInfo = new NotificationResponse.ActorInfo(1L, "old", "Old Name");
        
        Long newId = 2L;
        String newUsername = "newuser";
        String newFullName = "New User";

        actorInfo.setId(newId);
        actorInfo.setUsername(newUsername);
        actorInfo.setFullName(newFullName);

        assertEquals(newId, actorInfo.getId());
        assertEquals(newUsername, actorInfo.getUsername());
        assertEquals(newFullName, actorInfo.getFullName());
    }

    @Test
    @DisplayName("Should create PostInfo correctly")
    void shouldCreatePostInfoCorrectly() {
        Long id = 1L;
        String description = "Test post description";

        NotificationResponse.PostInfo postInfo = new NotificationResponse.PostInfo(id, description);

        assertEquals(id, postInfo.getId());
        assertEquals(description, postInfo.getDescription());
    }

    @Test
    @DisplayName("Should set and get PostInfo properties")
    void shouldSetAndGetPostInfoProperties() {
        NotificationResponse.PostInfo postInfo = new NotificationResponse.PostInfo(1L, "Old description");
        
        Long newId = 2L;
        String newDescription = "New description";

        postInfo.setId(newId);
        postInfo.setDescription(newDescription);

        assertEquals(newId, postInfo.getId());
        assertEquals(newDescription, postInfo.getDescription());
    }

    @Test
    @DisplayName("Should create CommentInfo correctly")
    void shouldCreateCommentInfoCorrectly() {
        Long id = 1L;
        String content = "Test comment content";

        NotificationResponse.CommentInfo commentInfo = new NotificationResponse.CommentInfo(id, content);

        assertEquals(id, commentInfo.getId());
        assertEquals(content, commentInfo.getContent());
    }

    @Test
    @DisplayName("Should set and get CommentInfo properties")
    void shouldSetAndGetCommentInfoProperties() {
        NotificationResponse.CommentInfo commentInfo = new NotificationResponse.CommentInfo(1L, "Old content");
        
        Long newId = 2L;
        String newContent = "New content";

        commentInfo.setId(newId);
        commentInfo.setContent(newContent);

        assertEquals(newId, commentInfo.getId());
        assertEquals(newContent, commentInfo.getContent());
    }
}