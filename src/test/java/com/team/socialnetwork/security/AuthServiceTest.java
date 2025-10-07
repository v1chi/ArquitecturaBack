package com.team.socialnetwork.security;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.team.socialnetwork.dto.LoginRequest;
import com.team.socialnetwork.dto.MessageResponse;
import com.team.socialnetwork.dto.RegisterRequest;
import com.team.socialnetwork.dto.TokenResponse;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.service.AuthService;
import com.team.socialnetwork.service.mail.MailService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private MailService mailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                mailService
        );
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setFullName("Test User");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(jwtService.generateEmailToken(request.getEmail())).thenReturn("email-token");
        doNothing().when(mailService).sendConfirmationEmail(eq(request.getEmail()), eq("email-token"));

        MessageResponse response = authService.register(request);

        assertEquals("Confirmation email was sent", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(mailService).sendConfirmationEmail(request.getEmail(), "email-token");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setUsername("testuser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertTrue(exception.getReason().contains("Email already in use"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("existinguser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertTrue(exception.getReason().contains("Username already in use"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email sending fails")
    void shouldThrowExceptionWhenEmailSendingFails() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setFullName("Test User");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(jwtService.generateEmailToken(request.getEmail())).thenReturn("email-token");
        doThrow(new RuntimeException("Email service error"))
                .when(mailService).sendConfirmationEmail(eq(request.getEmail()), eq("email-token"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertTrue(exception.getReason().contains("Failed to send confirmation email"));
    }

    @Test
    @DisplayName("Should login successfully with confirmed email")
    void shouldLoginSuccessfullyWithConfirmedEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setFullName("Test User");
        user.setEmailConfirmed(true);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(eq(request.getEmail()), any())).thenReturn("access-token");

        TokenResponse response = authService.login(request);

        assertEquals("access-token", response.getAccess_token());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertTrue(exception.getReason().contains("Invalid email or password"));
    }

    @Test
    @DisplayName("Should throw exception when password is wrong")
    void shouldThrowExceptionWhenPasswordWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertTrue(exception.getReason().contains("Invalid email or password"));
    }

    @Test
    @DisplayName("Should throw exception when email not confirmed")
    void shouldThrowExceptionWhenEmailNotConfirmed() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setEmailConfirmed(false);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertTrue(exception.getReason().contains("Email not confirmed"));
    }

    @Test
    @DisplayName("Should confirm email successfully")
    void shouldConfirmEmailSuccessfully() {
        String token = "valid-email-token";
        String email = "test@example.com";

        User user = new User();
        user.setEmail(email);
        user.setEmailConfirmed(false);

        when(jwtService.isEmailToken(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        MessageResponse response = authService.confirmEmail(token);

        assertEquals("Email confirmed", response.getMessage());
        assertTrue(user.isEmailConfirmed());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw exception for invalid email token")
    void shouldThrowExceptionForInvalidEmailToken() {
        String token = "invalid-token";

        when(jwtService.isEmailToken(token)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.confirmEmail(token)
        );

        assertTrue(exception.getReason().contains("Invalid or expired token"));
    }

    @Test
    @DisplayName("Should request password reset successfully")
    void shouldRequestPasswordResetSuccessfully() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generatePasswordResetToken(email)).thenReturn("reset-token");
        doNothing().when(mailService).sendPasswordReset(eq(email), eq("reset-token"));

        MessageResponse response = authService.requestPasswordReset(email);

        assertEquals("If the email exists, instructions were sent", response.getMessage());
        verify(mailService).sendPasswordReset(email, "reset-token");
    }

    @Test
    @DisplayName("Should reset password successfully")
    void shouldResetPasswordSuccessfully() {
        String token = "valid-reset-token";
        String email = "test@example.com";
        String newPassword = "newpassword123";

        User user = new User();
        user.setEmail(email);

        when(jwtService.isPasswordResetToken(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded-new-password");

        MessageResponse response = authService.resetPassword(token, newPassword);

        assertEquals("Password reset successfully", response.getMessage());
        verify(userRepository).save(user);
        verify(passwordEncoder).encode(newPassword);
    }
}