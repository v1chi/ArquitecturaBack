package com.team.socialnetwork.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MessageResponse DTO Tests")
class MessageResponseTest {

    @Test
    @DisplayName("Should create MessageResponse with default constructor")
    void shouldCreateMessageResponseWithDefaultConstructor() {
        MessageResponse response = new MessageResponse();

        assertNull(response.getMessage());
    }

    @Test
    @DisplayName("Should create MessageResponse with parameterized constructor")
    void shouldCreateMessageResponseWithParameterizedConstructor() {
        String message = "Test message";

        MessageResponse response = new MessageResponse(message);

        assertEquals(message, response.getMessage());
    }

    @Test
    @DisplayName("Should set and get message correctly")
    void shouldSetAndGetMessageCorrectly() {
        MessageResponse response = new MessageResponse();
        String message = "New test message";

        response.setMessage(message);

        assertEquals(message, response.getMessage());
    }
}