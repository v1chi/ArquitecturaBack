package com.team.socialnetwork.dto;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ChatMessageResponseTest {

    @Test
    void testParameterizedConstructor() {
        Long id = 1L;
        Long senderId = 2L;
        Long receiverId = 3L;
        String content = "Hello World";
        Instant createdAt = Instant.now();
        boolean isRead = true;

        ChatMessageResponse response = new ChatMessageResponse(id, senderId, receiverId, content, createdAt, isRead);

        assertEquals(id, response.getId());
        assertEquals(senderId, response.getSenderId());
        assertEquals(receiverId, response.getReceiverId());
        assertEquals(content, response.getContent());
        assertEquals(createdAt, response.getCreatedAt());
        assertTrue(response.isRead());
    }

    @Test
    void testGetId() {
        Long id = 123L;
        ChatMessageResponse response = new ChatMessageResponse(id, 1L, 2L, "test", Instant.now(), false);
        
        assertEquals(id, response.getId());
    }

    @Test
    void testGetSenderId() {
        Long senderId = 456L;
        ChatMessageResponse response = new ChatMessageResponse(1L, senderId, 2L, "test", Instant.now(), false);
        
        assertEquals(senderId, response.getSenderId());
    }

    @Test
    void testGetReceiverId() {
        Long receiverId = 789L;
        ChatMessageResponse response = new ChatMessageResponse(1L, 2L, receiverId, "test", Instant.now(), false);
        
        assertEquals(receiverId, response.getReceiverId());
    }

    @Test
    void testGetContent() {
        String content = "This is a test message";
        ChatMessageResponse response = new ChatMessageResponse(1L, 2L, 3L, content, Instant.now(), false);
        
        assertEquals(content, response.getContent());
    }

    @Test
    void testGetCreatedAt() {
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        ChatMessageResponse response = new ChatMessageResponse(1L, 2L, 3L, "test", createdAt, false);
        
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testIsRead() {
        ChatMessageResponse readMessage = new ChatMessageResponse(1L, 2L, 3L, "test", Instant.now(), true);
        ChatMessageResponse unreadMessage = new ChatMessageResponse(2L, 3L, 4L, "test2", Instant.now(), false);
        
        assertTrue(readMessage.isRead());
        assertFalse(unreadMessage.isRead());
    }

    @Test
    void testWithNullValues() {
        ChatMessageResponse response = new ChatMessageResponse(null, null, null, null, null, false);
        
        assertNull(response.getId());
        assertNull(response.getSenderId());
        assertNull(response.getReceiverId());
        assertNull(response.getContent());
        assertNull(response.getCreatedAt());
        assertFalse(response.isRead());
    }

    @Test
    void testWithEmptyContent() {
        ChatMessageResponse response = new ChatMessageResponse(1L, 2L, 3L, "", Instant.now(), false);
        
        assertEquals("", response.getContent());
    }
}