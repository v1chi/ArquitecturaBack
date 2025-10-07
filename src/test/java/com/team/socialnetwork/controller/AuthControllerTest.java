package com.team.socialnetwork.controller;

import com.team.socialnetwork.dto.*;
import com.team.socialnetwork.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testRegister() throws Exception {
        MessageResponse response = new MessageResponse("User registered successfully");
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"username\":\"johndoe\",\"email\":\"john@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void testLogin() throws Exception {
        TokenResponse response = new TokenResponse("fake-jwt-token");
        
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void testConfirmEmail() throws Exception {
        MessageResponse response = new MessageResponse("Email confirmed successfully");
        
        when(authService.confirmEmail(anyString())).thenReturn(response);

        mockMvc.perform(get("/auth/confirm-email")
                .param("token", "fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email confirmed successfully"));
    }

    @Test
    void testRequestPasswordReset() throws Exception {
        MessageResponse response = new MessageResponse("Password reset email sent");
        
        when(authService.requestPasswordReset(anyString())).thenReturn(response);

        mockMvc.perform(post("/auth/request-password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent"));
    }

    @Test
    void testResetPassword() throws Exception {
        MessageResponse response = new MessageResponse("Password reset successfully");
        
        when(authService.resetPassword(anyString(), anyString())).thenReturn(response);

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"fake-token\",\"newPassword\":\"newPassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }
}