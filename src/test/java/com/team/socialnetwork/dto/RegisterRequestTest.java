package com.team.socialnetwork.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RegisterRequest DTO Tests")
class RegisterRequestTest {

    @Test
    @DisplayName("Should create RegisterRequest with default constructor")
    void shouldCreateRegisterRequestWithDefaultConstructor() {
        RegisterRequest request = new RegisterRequest();

        assertNull(request.getUsername());
        assertNull(request.getFullName());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    @DisplayName("Should set and get username correctly")
    void shouldSetAndGetUsernameCorrectly() {
        RegisterRequest request = new RegisterRequest();
        String username = "testuser";

        request.setUsername(username);

        assertEquals(username, request.getUsername());
    }

    @Test
    @DisplayName("Should set and get fullName correctly")
    void shouldSetAndGetFullNameCorrectly() {
        RegisterRequest request = new RegisterRequest();
        String fullName = "Test User";

        request.setFullName(fullName);

        assertEquals(fullName, request.getFullName());
    }

    @Test
    @DisplayName("Should set and get email correctly")
    void shouldSetAndGetEmailCorrectly() {
        RegisterRequest request = new RegisterRequest();
        String email = "test@example.com";

        request.setEmail(email);

        assertEquals(email, request.getEmail());
    }

    @Test
    @DisplayName("Should set and get password correctly")
    void shouldSetAndGetPasswordCorrectly() {
        RegisterRequest request = new RegisterRequest();
        String password = "password123";

        request.setPassword(password);

        assertEquals(password, request.getPassword());
    }

    @Test
    @DisplayName("Should set all properties correctly")
    void shouldSetAllPropertiesCorrectly() {
        RegisterRequest request = new RegisterRequest();
        String username = "testuser";
        String fullName = "Test User";
        String email = "test@example.com";
        String password = "password123";

        request.setUsername(username);
        request.setFullName(fullName);
        request.setEmail(email);
        request.setPassword(password);

        assertEquals(username, request.getUsername());
        assertEquals(fullName, request.getFullName());
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
    }
}