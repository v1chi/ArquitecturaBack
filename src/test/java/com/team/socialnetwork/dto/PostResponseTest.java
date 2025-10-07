package com.team.socialnetwork.dto;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PostResponse DTO Tests")
class PostResponseTest {

    @Test
    @DisplayName("Should create PostResponse with default constructor")
    void shouldCreatePostResponseWithDefaultConstructor() {
        PostResponse response = new PostResponse();

        assertNull(response.getId());
        assertNull(response.getCreatedAt());
        assertNull(response.getDescription());
        assertNull(response.getImage());
        assertNull(response.getUsername());
        assertNull(response.getUserId());
    }

    @Test
    @DisplayName("Should create PostResponse with parameterized constructor")
    void shouldCreatePostResponseWithParameterizedConstructor() {
        Long id = 1L;
        Instant createdAt = Instant.now();
        String description = "Test post description";
        String image = "test-image.jpg";
        String username = "testuser";
        Long userId = 123L;

        PostResponse response = new PostResponse(id, createdAt, description, image, username, userId);

        assertEquals(id, response.getId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(description, response.getDescription());
        assertEquals(image, response.getImage());
        assertEquals(username, response.getUsername());
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        PostResponse response = new PostResponse();
        Long id = 1L;
        Instant createdAt = Instant.now();
        String description = "Updated description";
        String image = "updated-image.jpg";
        String username = "updateduser";
        Long userId = 456L;

        response.setId(id);
        response.setCreatedAt(createdAt);
        response.setDescription(description);
        response.setImage(image);
        response.setUsername(username);
        response.setUserId(userId);

        assertEquals(id, response.getId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(description, response.getDescription());
        assertEquals(image, response.getImage());
        assertEquals(username, response.getUsername());
        assertEquals(userId, response.getUserId());
    }
}