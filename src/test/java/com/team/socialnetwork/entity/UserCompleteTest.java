package com.team.socialnetwork.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Should create User with default constructor")
    void shouldCreateUserWithDefaultConstructor() {
        // When
        User newUser = new User();

        // Then
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getEmail());
        assertNull(newUser.getPassword());
        assertNull(newUser.getFullName());
        assertFalse(newUser.isEmailConfirmed());
        assertFalse(newUser.isPrivate());
        assertNull(newUser.getProfilePicture());
        assertNotNull(newUser.getFollowing());
        assertNotNull(newUser.getFollowers());
        assertTrue(newUser.getFollowing().isEmpty());
        assertTrue(newUser.getFollowers().isEmpty());
    }

    @Test
    @DisplayName("Should set and get all basic properties correctly")
    void shouldSetAndGetAllBasicPropertiesCorrectly() {
        // Given
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String password = "encodedPassword";
        String fullName = "Test User";
        String profilePicture = "profile.jpg";
        boolean emailConfirmed = true;
        boolean isPrivate = true;

        // When
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setProfilePicture(profilePicture);
        user.setEmailConfirmed(emailConfirmed);
        user.setPrivate(isPrivate);

        // Then
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(fullName, user.getFullName());
        assertEquals(profilePicture, user.getProfilePicture());
        assertTrue(user.isEmailConfirmed());
        assertTrue(user.isPrivate());
    }

    @Test
    @DisplayName("Should handle following and followers relationships")
    void shouldHandleFollowingAndFollowersRelationships() {
        // Given
        User followedUser = new User();
        followedUser.setId(2L);
        followedUser.setUsername("followed");

        User followerUser = new User();
        followerUser.setId(3L);
        followerUser.setUsername("follower");

        Set<User> following = new HashSet<>();
        following.add(followedUser);

        Set<User> followers = new HashSet<>();
        followers.add(followerUser);

        // When
        user.setFollowing(following);
        user.setFollowers(followers);

        // Then
        assertEquals(1, user.getFollowing().size());
        assertEquals(1, user.getFollowers().size());
        assertTrue(user.getFollowing().contains(followedUser));
        assertTrue(user.getFollowers().contains(followerUser));
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void shouldHandleNullValuesAppropriately() {
        // When
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);
        user.setPassword(null);
        user.setFullName(null);
        user.setProfilePicture(null);
        user.setFollowing(null);
        user.setFollowers(null);

        // Then
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getFullName());
        assertNull(user.getProfilePicture());
        assertNull(user.getFollowing());
        assertNull(user.getFollowers());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void shouldHandleEmptyStrings() {
        // When
        user.setUsername("");
        user.setEmail("");
        user.setPassword("");
        user.setFullName("");
        user.setProfilePicture("");

        // Then
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
        assertEquals("", user.getFullName());
        assertEquals("", user.getProfilePicture());
    }

    @Test
    @DisplayName("Should handle boolean flags correctly")
    void shouldHandleBooleanFlagsCorrectly() {
        // Initially false
        assertFalse(user.isEmailConfirmed());
        assertFalse(user.isPrivate());

        // Set to true
        user.setEmailConfirmed(true);
        user.setPrivate(true);
        assertTrue(user.isEmailConfirmed());
        assertTrue(user.isPrivate());

        // Set back to false
        user.setEmailConfirmed(false);
        user.setPrivate(false);
        assertFalse(user.isEmailConfirmed());
        assertFalse(user.isPrivate());
    }

    @Test
    @DisplayName("Should initialize collections properly")
    void shouldInitializeCollectionsProperly() {
        // When
        User newUser = new User();

        // Then
        assertNotNull(newUser.getFollowing());
        assertNotNull(newUser.getFollowers());
        assertTrue(newUser.getFollowing().isEmpty());
        assertTrue(newUser.getFollowers().isEmpty());

        // Should be able to add to collections
        User otherUser = new User();
        newUser.getFollowing().add(otherUser);
        newUser.getFollowers().add(otherUser);

        assertEquals(1, newUser.getFollowing().size());
        assertEquals(1, newUser.getFollowers().size());
    }
}