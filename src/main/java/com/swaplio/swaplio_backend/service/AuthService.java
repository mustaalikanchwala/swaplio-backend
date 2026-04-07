package com.swaplio.swaplio_backend.service;

// service/AuthService.java
import com.swaplio.swaplio_backend.dto.auth.AuthResponse;
import com.swaplio.swaplio_backend.dto.auth.LoginRequest;
import com.swaplio.swaplio_backend.dto.auth.RegisterRequest;
import com.swaplio.swaplio_backend.exception.EmailAlreadyRegisterException;
import com.swaplio.swaplio_backend.exception.InavlidCredentialsException;
import com.swaplio.swaplio_backend.model.User;
import com.swaplio.swaplio_backend.repository.UserRepository;
import com.swaplio.swaplio_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisterException("Email already registered");
        }
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .institution(request.institution())
                .build();
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InavlidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InavlidCredentialsException("Invalid email or password");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }
}