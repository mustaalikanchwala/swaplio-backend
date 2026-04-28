// exception/GlobalExceptionHandler.java
package com.swaplio.swaplio_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Generic structure every error response follows ──────────
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    // ─── 404 — resource not found ─────────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage();
        if (msg != null && (msg.contains("not found") || msg.contains("Not Found"))) {
            return buildError(HttpStatus.NOT_FOUND, msg);
        }
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, msg);
    }

    // ─── 403 — unauthorized action ────────────────────────────────
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // ─── 400 — validation errors (@Valid failures) ────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(err.getField(), err.getDefaultMessage());
        }

        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("fields", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // ─── 413 — file too large ─────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileTooLarge(
            MaxUploadSizeExceededException ex) {
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE,
                "File too large. Maximum allowed size is 10MB.");
    }

    // ─── 400 — illegal arguments ─────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(
            IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ─── 409 — Conflict ───────────────────────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,Object>> handleIllegalState(
            IllegalStateException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    // CUSTOM EXCEPTION HANDLING
    @ExceptionHandler(EmailAlreadyRegisterException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyRegister(
            EmailAlreadyRegisterException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(
            CategoryNotFoundException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(InavlidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInavlidCred(
            InavlidCredentialsException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleListingNotFound(
            ListingNotFoundException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(MeetingNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleMeetingNotFound(
            MeetingNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

}