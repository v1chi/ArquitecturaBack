package com.team.socialnetwork.security;

import java.time.Instant;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.team.socialnetwork.entity.User;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with constructor")
    void shouldCreateUserWithConstructor() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";

        User user = new User(username, email, password);

        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertFalse(user.isEmailConfirmed());
        assertFalse(user.isPrivate());
    }

    @Test
    @DisplayName("Should create user with default constructor")
    void shouldCreateUserWithDefaultConstructor() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertFalse(user.isEmailConfirmed());
        assertFalse(user.isPrivate());
        assertNotNull(user.getPosts());
        assertNotNull(user.getComments());
        assertNotNull(user.getLikes());
        assertNotNull(user.getFollowing());
        assertNotNull(user.getFollowers());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        User user = new User();
        Long id = 1L;
        Instant createdAt = Instant.now();
        String username = "testuser";
        String fullName = "Test User";
        String email = "test@example.com";
        String password = "password123";

        user.setId(id);
        user.setCreatedAt(createdAt);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setEmailConfirmed(true);
        user.setPrivate(true);

        assertEquals(id, user.getId());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(username, user.getUsername());
        assertEquals(fullName, user.getFullName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertTrue(user.isEmailConfirmed());
        assertTrue(user.isPrivate());
    }

    @Test
    @DisplayName("Should handle following relationships")
    void shouldHandleFollowingRelationships() {
        User user1 = new User("user1", "user1@example.com", "password");
        User user2 = new User("user2", "user2@example.com", "password");
        User user3 = new User("user3", "user3@example.com", "password");

        user1.setFollowing(new HashSet<>());
        user1.setFollowers(new HashSet<>());

        user1.getFollowing().add(user2);
        user1.getFollowing().add(user3);

        assertEquals(2, user1.getFollowing().size());
        assertTrue(user1.getFollowing().contains(user2));
        assertTrue(user1.getFollowing().contains(user3));
    }

    @Test
    @DisplayName("Should initialize collections properly")
    void shouldInitializeCollectionsProperly() {
        User user = new User();

        assertNotNull(user.getPosts());
        assertNotNull(user.getComments());
        assertNotNull(user.getLikes());
        assertNotNull(user.getFollowing());
        assertNotNull(user.getFollowers());
        
        assertTrue(user.getPosts().isEmpty());
        assertTrue(user.getComments().isEmpty());
        assertTrue(user.getLikes().isEmpty());
        assertTrue(user.getFollowing().isEmpty());
        assertTrue(user.getFollowers().isEmpty());
    }
}