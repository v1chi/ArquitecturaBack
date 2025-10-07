package com.team.socialnetwork.security;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Filter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("Should continue filter chain when no Authorization header")
    void shouldContinueFilterChainWhenNoAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header doesn't start with Bearer")
    void shouldContinueFilterChainWhenNotBearerToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdA==");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Should continue filter chain when token extraction fails")
    void shouldContinueFilterChainWhenTokenExtractionFails() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.extractSubject("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractSubject("invalid-token");
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Should authenticate user when valid token provided")
    void shouldAuthenticateUserWhenValidToken() throws ServletException, IOException {
        String token = "valid-jwt-token";
        String email = "test@example.com";
        
        UserDetails userDetails = User.builder()
                .username(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, email)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractSubject(token);
        verify(userDetailsService).loadUserByUsername(email);
        verify(jwtService).isTokenValid(token, email);
        
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("Should not authenticate when token is invalid")
    void shouldNotAuthenticateWhenTokenInvalid() throws ServletException, IOException {
        String token = "invalid-jwt-token";
        String email = "test@example.com";
        
        UserDetails userDetails = User.builder()
                .username(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, email)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractSubject(token);
        verify(userDetailsService).loadUserByUsername(email);
        verify(jwtService).isTokenValid(token, email);
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should not authenticate when user already authenticated")
    void shouldNotAuthenticateWhenUserAlreadyAuthenticated() throws ServletException, IOException {
        String token = "valid-jwt-token";
        String email = "test@example.com";

        // Set up existing authentication
        UserDetails existingUser = User.builder()
                .username("existing@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        existingUser, null, existingUser.getAuthorities()));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractSubject(token)).thenReturn(email);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractSubject(token);
        verifyNoInteractions(userDetailsService);
        
        // Authentication should remain unchanged
        assertEquals("existing@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("Should continue filter chain when subject is null")
    void shouldContinueFilterChainWhenSubjectNull() throws ServletException, IOException {
        String token = "token-without-subject";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractSubject(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractSubject(token);
        verifyNoInteractions(userDetailsService);
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}