package com.team.socialnetwork.dto;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class FollowRequestResponseTest {

    @Test
    void testDefaultConstructor() {
        FollowRequestResponse response = new FollowRequestResponse();
        
        assertNull(response.getId());
        assertNull(response.getFollowerId());
        assertNull(response.getFollowerUsername());
        assertNull(response.getFollowerFullName());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testParameterizedConstructor() {
        Long id = 1L;
        Long followerId = 2L;
        String followerUsername = "johndoe";
        String followerFullName = "John Doe";
        Instant createdAt = Instant.now();

        FollowRequestResponse response = new FollowRequestResponse(id, followerId, followerUsername, followerFullName, createdAt);

        assertEquals(id, response.getId());
        assertEquals(followerId, response.getFollowerId());
        assertEquals(followerUsername, response.getFollowerUsername());
        assertEquals(followerFullName, response.getFollowerFullName());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        FollowRequestResponse response = new FollowRequestResponse();
        Long id = 123L;
        
        response.setId(id);
        assertEquals(id, response.getId());
    }

    @Test
    void testFollowerIdGetterAndSetter() {
        FollowRequestResponse response = new FollowRequestResponse();
        Long followerId = 456L;
        
        response.setFollowerId(followerId);
        assertEquals(followerId, response.getFollowerId());
    }

    @Test
    void testFollowerUsernameGetterAndSetter() {
        FollowRequestResponse response = new FollowRequestResponse();
        String username = "testuser";
        
        response.setFollowerUsername(username);
        assertEquals(username, response.getFollowerUsername());
    }

    @Test
    void testFollowerFullNameGetterAndSetter() {
        FollowRequestResponse response = new FollowRequestResponse();
        String fullName = "Test User";
        
        response.setFollowerFullName(fullName);
        assertEquals(fullName, response.getFollowerFullName());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        FollowRequestResponse response = new FollowRequestResponse();
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        
        response.setCreatedAt(createdAt);
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testWithNullValues() {
        FollowRequestResponse response = new FollowRequestResponse(null, null, null, null, null);
        
        assertNull(response.getId());
        assertNull(response.getFollowerId());
        assertNull(response.getFollowerUsername());
        assertNull(response.getFollowerFullName());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testWithEmptyStrings() {
        FollowRequestResponse response = new FollowRequestResponse();
        response.setFollowerUsername("");
        response.setFollowerFullName("");
        
        assertEquals("", response.getFollowerUsername());
        assertEquals("", response.getFollowerFullName());
    }

    @Test
    void testSettersWithNull() {
        FollowRequestResponse response = new FollowRequestResponse(1L, 2L, "user", "User Name", Instant.now());
        
        response.setId(null);
        response.setFollowerId(null);
        response.setFollowerUsername(null);
        response.setFollowerFullName(null);
        response.setCreatedAt(null);
        
        assertNull(response.getId());
        assertNull(response.getFollowerId());
        assertNull(response.getFollowerUsername());
        assertNull(response.getFollowerFullName());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testRealWorldExample() {
        FollowRequestResponse response = new FollowRequestResponse(
            100L, 
            50L, 
            "jane_smith", 
            "Jane Smith", 
            Instant.parse("2024-05-15T14:30:00Z")
        );
        
        assertEquals(100L, response.getId());
        assertEquals(50L, response.getFollowerId());
        assertEquals("jane_smith", response.getFollowerUsername());
        assertEquals("Jane Smith", response.getFollowerFullName());
        assertEquals(Instant.parse("2024-05-15T14:30:00Z"), response.getCreatedAt());
    }
}