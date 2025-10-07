package com.team.socialnetwork.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LoginRequest DTO Tests")
class LoginRequestTest {

    @Test
    @DisplayName("Should create LoginRequest with default constructor")
    void shouldCreateLoginRequestWithDefaultConstructor() {
        LoginRequest request = new LoginRequest();

        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    @DisplayName("Should set and get email correctly")
    void shouldSetAndGetEmailCorrectly() {
        LoginRequest request = new LoginRequest();
        String email = "test@example.com";

        request.setEmail(email);

        assertEquals(email, request.getEmail());
    }

    @Test
    @DisplayName("Should set and get password correctly")
    void shouldSetAndGetPasswordCorrectly() {
        LoginRequest request = new LoginRequest();
        String password = "password123";

        request.setPassword(password);

        assertEquals(password, request.getPassword());
    }

    @Test
    @DisplayName("Should set all properties correctly")
    void shouldSetAllPropertiesCorrectly() {
        LoginRequest request = new LoginRequest();
        String email = "test@example.com";
        String password = "password123";

        request.setEmail(email);
        request.setPassword(password);

        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
    }
}