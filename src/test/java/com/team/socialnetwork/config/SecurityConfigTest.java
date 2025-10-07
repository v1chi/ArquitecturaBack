package com.team.socialnetwork.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testSecurityFilterChainBeanExists() {
        SecurityFilterChain securityFilterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertNotNull(securityFilterChain);
    }

    @Test
    void testPasswordEncoderBeanExists() {
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        assertNotNull(passwordEncoder);
    }

    @Test
    void testPasswordEncoderWorks() {
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testSecurityConfigBeanExists() {
        SecurityConfig securityConfig = applicationContext.getBean(SecurityConfig.class);
        assertNotNull(securityConfig);
    }
}