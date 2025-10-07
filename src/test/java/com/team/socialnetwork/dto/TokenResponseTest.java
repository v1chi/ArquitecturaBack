package com.team.socialnetwork.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TokenResponse DTO Tests")
class TokenResponseTest {

    @Test
    @DisplayName("Should create TokenResponse with default constructor")
    void shouldCreateTokenResponseWithDefaultConstructor() {
        TokenResponse response = new TokenResponse();

        assertNull(response.getAccess_token());
    }

    @Test
    @DisplayName("Should create TokenResponse with parameterized constructor")
    void shouldCreateTokenResponseWithParameterizedConstructor() {
        String accessToken = "test-access-token";

        TokenResponse response = new TokenResponse(accessToken);

        assertEquals(accessToken, response.getAccess_token());
    }

    @Test
    @DisplayName("Should set and get access_token correctly")
    void shouldSetAndGetAccessTokenCorrectly() {
        TokenResponse response = new TokenResponse();
        String accessToken = "new-access-token";

        response.setAccess_token(accessToken);

        assertEquals(accessToken, response.getAccess_token());
    }
}