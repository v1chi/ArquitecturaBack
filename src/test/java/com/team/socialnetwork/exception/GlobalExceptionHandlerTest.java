package com.team.socialnetwork.exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle ResponseStatusException and return proper error response")
    void shouldHandleResponseStatusExceptionAndReturnProperErrorResponse() {
        // Given
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleResponseStatusException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("User not found", body.get("message"));
        assertEquals("404 NOT_FOUND", body.get("error"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("Should handle ResponseStatusException with null reason")
    void shouldHandleResponseStatusExceptionWithNullReason() {
        // Given
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleResponseStatusException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertNull(body.get("message"));
        assertEquals("400 BAD_REQUEST", body.get("error"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return validation error")
    void shouldHandleMethodArgumentNotValidExceptionAndReturnValidationError() {
        // Given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.addError(new FieldError("test", "email", "Email is required"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidation(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Email is required", body.get("message"));
        assertEquals("VALIDATION_ERROR", body.get("error"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with no field errors")
    void shouldHandleMethodArgumentNotValidExceptionWithNoFieldErrors() {
        // Given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidation(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Validation error", body.get("message"));
        assertEquals("VALIDATION_ERROR", body.get("error"));
    }

    @Test
    @DisplayName("Should handle generic Exception and return internal server error")
    void shouldHandleGenericExceptionAndReturnInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Something went wrong");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneric(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Unexpected error", body.get("message"));
        assertEquals("INTERNAL_ERROR", body.get("error"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("Should handle null pointer exception")
    void shouldHandleNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("Null reference");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneric(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Unexpected error", body.get("message"));
        assertEquals("INTERNAL_ERROR", body.get("error"));
    }
}