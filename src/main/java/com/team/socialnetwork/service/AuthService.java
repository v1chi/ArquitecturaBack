package com.team.socialnetwork.service;

import com.team.socialnetwork.dto.TokenResponse;
import com.team.socialnetwork.dto.LoginRequest;
import com.team.socialnetwork.dto.RegisterRequest;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.security.JwtService;
import com.team.socialnetwork.service.mail.MailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.mailService = mailService;
    }

    @Transactional
    public com.team.socialnetwork.dto.MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Initial state: not verified
        user.setEmailConfirmed(false);
        userRepository.save(user);

        try {
            String emailToken = jwtService.generateEmailToken(user.getEmail());
            mailService.sendConfirmationEmail(user.getEmail(), emailToken);
        } catch (Exception e) {
            // Fail registration if we cannot notify user
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send confirmation email"
            );
        }

        return new com.team.socialnetwork.dto.MessageResponse("Confirmation email was sent");
    }

    public TokenResponse login(LoginRequest request) {
        // 1) Validate credentials
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        boolean passwordOk = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordOk) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // 2) Enforce email confirmation
        if (!user.isEmailConfirmed()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Email not confirmed");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", user.getName());
        claims.put("userId", user.getId());

        String access = jwtService.generateAccessToken(user.getEmail(), claims);
        return new TokenResponse(access);
    }

    @Transactional
    public com.team.socialnetwork.dto.MessageResponse confirmEmail(String token) {
        if (!jwtService.isEmailToken(token)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }
        String email;
        try {
            email = jwtService.extractSubject(token);
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        user.setEmailConfirmed(true);
        userRepository.save(user);
        return new com.team.socialnetwork.dto.MessageResponse("Email confirmed");
    }
}
