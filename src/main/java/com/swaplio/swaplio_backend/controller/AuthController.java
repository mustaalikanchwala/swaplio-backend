package com.swaplio.swaplio_backend.controller;

// controller/AuthController.java

import com.swaplio.swaplio_backend.dto.auth.AuthResponse;
import com.swaplio.swaplio_backend.dto.auth.LoginRequest;
import com.swaplio.swaplio_backend.dto.auth.RegisterRequest;
import com.swaplio.swaplio_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}