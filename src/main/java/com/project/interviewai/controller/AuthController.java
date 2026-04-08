package com.project.interviewai.controller;

import com.project.interviewai.dto.AuthResponse;
import com.project.interviewai.dto.LoginRequest;
import com.project.interviewai.dto.RegisterRequest;
import com.project.interviewai.model.User;
import com.project.interviewai.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Register a new user with email and password
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login 
     * Authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/me
     * Get current authenticated user's profile
     * Requires valid JWT token in Authorization header
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());

        Map<String, Object> profile = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "role", user.getRole(),
                "targetRole", user.getTargetRole() != null ? user.getTargetRole() : "",
                "experienceYears", user.getExperienceYears(),
                "createdAt", user.getCreatedAt().toString()
        );

        return ResponseEntity.ok(profile);
    }
}
