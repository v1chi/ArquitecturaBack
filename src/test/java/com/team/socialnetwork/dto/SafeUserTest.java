package com.team.socialnetwork.dto;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class SafeUserTest {

    @Test
    void testDefaultConstructor() {
        SafeUser user = new SafeUser();
        
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getCreatedAt());
    }

    @Test
    void testParameterizedConstructor() {
        Long id = 1L;
        String name = "John Doe";
        String username = "johndoe";
        String email = "john@example.com";
        Instant createdAt = Instant.now();

        SafeUser user = new SafeUser(id, name, username, email, createdAt);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        SafeUser user = new SafeUser();
        Long id = 123L;
        
        user.setId(id);
        assertEquals(id, user.getId());
    }

    @Test
    void testNameGetterAndSetter() {
        SafeUser user = new SafeUser();
        String name = "Jane Smith";
        
        user.setName(name);
        assertEquals(name, user.getName());
    }

    @Test
    void testUsernameGetterAndSetter() {
        SafeUser user = new SafeUser();
        String username = "janesmith";
        
        user.setUsername(username);
        assertEquals(username, user.getUsername());
    }

    @Test
    void testEmailGetterAndSetter() {
        SafeUser user = new SafeUser();
        String email = "jane@example.com";
        
        user.setEmail(email);
        assertEquals(email, user.getEmail());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        SafeUser user = new SafeUser();
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        
        user.setCreatedAt(createdAt);
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void testWithNullValues() {
        SafeUser user = new SafeUser(null, null, null, null, null);
        
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getCreatedAt());
    }

    @Test
    void testWithEmptyStrings() {
        SafeUser user = new SafeUser();
        user.setName("");
        user.setUsername("");
        user.setEmail("");
        
        assertEquals("", user.getName());
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
    }

    @Test
    void testSettersWithNull() {
        SafeUser user = new SafeUser(1L, "John", "john", "john@test.com", Instant.now());
        
        user.setId(null);
        user.setName(null);
        user.setUsername(null);
        user.setEmail(null);
        user.setCreatedAt(null);
        
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getCreatedAt());
    }
}