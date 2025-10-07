package com.team.socialnetwork.dto;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CommentResponse DTO Tests")
class CommentResponseTest {

    @Test
    @DisplayName("Should create CommentResponse with default constructor")
    void shouldCreateCommentResponseWithDefaultConstructor() {
        CommentResponse response = new CommentResponse();

        assertNull(response.getId());
        assertNull(response.getCreatedAt());
        assertNull(response.getText());
        assertNull(response.getUsername());
    }

    @Test
    @DisplayName("Should create CommentResponse with parameterized constructor")
    void shouldCreateCommentResponseWithParameterizedConstructor() {
        Long id = 1L;
        Instant createdAt = Instant.now();
        String text = "Test comment text";
        String username = "testuser";

        CommentResponse response = new CommentResponse(id, createdAt, text, username);

        assertEquals(id, response.getId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(text, response.getText());
        assertEquals(username, response.getUsername());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        CommentResponse response = new CommentResponse();
        Long id = 2L;
        Instant createdAt = Instant.now();
        String text = "Updated comment text";
        String username = "updateduser";

        response.setId(id);
        response.setCreatedAt(createdAt);
        response.setText(text);
        response.setUsername(username);

        assertEquals(id, response.getId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(text, response.getText());
        assertEquals(username, response.getUsername());
    }
}