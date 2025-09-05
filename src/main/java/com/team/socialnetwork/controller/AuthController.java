package com.team.socialnetwork.controller;

import com.team.socialnetwork.dto.TokenResponse;
import com.team.socialnetwork.dto.LoginRequest;
import com.team.socialnetwork.dto.RegisterRequest;
import com.team.socialnetwork.dto.MessageResponse;
import com.team.socialnetwork.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<MessageResponse> confirmEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.confirmEmail(token));
    }
}
