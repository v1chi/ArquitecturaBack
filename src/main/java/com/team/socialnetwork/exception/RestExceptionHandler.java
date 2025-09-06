package com.team.socialnetwork.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Duplicate value violates unique constraint";
        String cause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        if (cause != null) {
            if (cause.contains("uk_users_email")) {
                message = "Email already in use";
            } else if (cause.contains("uk_users_name")) {
                message = "Name already in use";
            }
        }
        Map<String, Object> body = new HashMap<>();
        body.put("error", "CONFLICT");
        body.put("message", message);
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}

