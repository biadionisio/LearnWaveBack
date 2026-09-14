package com.example.learnwave.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", HttpStatus.FORBIDDEN.value(),
                "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", 500);
        error.put("error", ex.getClass().getSimpleName());
        error.put("message", ex.getMessage());
        
        // Pegar causa raiz
        Throwable cause = ex.getCause();
        if (cause != null) {
            error.put("cause", cause.getClass().getSimpleName());
            error.put("causeMessage", cause.getMessage());
            
            Throwable rootCause = cause.getCause();
            if (rootCause != null) {
                error.put("rootCause", rootCause.getClass().getSimpleName());
                error.put("rootCauseMessage", rootCause.getMessage());
            }
        }

        // Log no console do Render
        System.err.println("=== ERRO CAPTURADO ===");
        System.err.println("Tipo: " + ex.getClass().getName());
        System.err.println("Mensagem: " + ex.getMessage());
        ex.printStackTrace();
        System.err.println("=== FIM ERRO ===");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
