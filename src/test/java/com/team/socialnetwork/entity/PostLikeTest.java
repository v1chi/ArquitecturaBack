package com.team.socialnetwork.entity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostLikeTest {

    private PostLike postLike;
    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        post = new Post();
        post.setId(1L);

        postLike = new PostLike();
    }

    @Test
    @DisplayName("Should create PostLike with default constructor")
    void shouldCreatePostLikeWithDefaultConstructor() {
        // When
        PostLike newPostLike = new PostLike();

        // Then
        assertNotNull(newPostLike);
        assertNull(newPostLike.getId());
        assertNull(newPostLike.getUser());
        assertNull(newPostLike.getPost());
        assertNull(newPostLike.getCreatedAt());
    }

    @Test
    @DisplayName("Should create PostLike with parameterized constructor")
    void shouldCreatePostLikeWithParameterizedConstructor() {
        // When
        PostLike newPostLike = new PostLike(user, post);

        // Then
        assertNotNull(newPostLike);
        assertEquals(user, newPostLike.getUser());
        assertEquals(post, newPostLike.getPost());
        assertNull(newPostLike.getId()); // Not set until persisted
        assertNull(newPostLike.getCreatedAt()); // Set by @CreationTimestamp on persist
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        // Given
        Long id = 1L;
        Instant createdAt = Instant.now();

        // When
        postLike.setId(id);
        postLike.setUser(user);
        postLike.setPost(post);
        postLike.setCreatedAt(createdAt);

        // Then
        assertEquals(id, postLike.getId());
        assertEquals(user, postLike.getUser());
        assertEquals(post, postLike.getPost());
        assertEquals(createdAt, postLike.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void shouldHandleNullValuesAppropriately() {
        // When
        postLike.setId(null);
        postLike.setUser(null);
        postLike.setPost(null);
        postLike.setCreatedAt(null);

        // Then
        assertNull(postLike.getId());
        assertNull(postLike.getUser());
        assertNull(postLike.getPost());
        assertNull(postLike.getCreatedAt());
    }

    @Test
    @DisplayName("Should maintain relationship between user and post")
    void shouldMaintainRelationshipBetweenUserAndPost() {
        // When
        postLike.setUser(user);
        postLike.setPost(post);

        // Then
        assertSame(user, postLike.getUser());
        assertSame(post, postLike.getPost());
        assertEquals(user.getId(), postLike.getUser().getId());
        assertEquals(post.getId(), postLike.getPost().getId());
    }

    @Test
    @DisplayName("Should allow updating user and post")
    void shouldAllowUpdatingUserAndPost() {
        // Given
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");

        Post newPost = new Post();
        newPost.setId(2L);

        postLike.setUser(user);
        postLike.setPost(post);

        // When
        postLike.setUser(newUser);
        postLike.setPost(newPost);

        // Then
        assertEquals(newUser, postLike.getUser());
        assertEquals(newPost, postLike.getPost());
        assertNotEquals(user, postLike.getUser());
        assertNotEquals(post, postLike.getPost());
    }
}