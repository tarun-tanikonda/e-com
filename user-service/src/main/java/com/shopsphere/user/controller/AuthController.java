package com.shopsphere.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.user.dto.request.LoginRequest;
import com.shopsphere.user.dto.response.LoginResponse;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.repository.UserRepository;
import com.shopsphere.user.security.JWTUtil;

import jakarta.validation.Valid;

/**
 * Authentication REST controller handling user login and token generation.
 * Supports both SRS paths (/api/auth/login) and versioned gateway paths (/api/v1/users/login).
 */
@RestController
@RequestMapping({"/api/v1/users", "/api/auth", "/api/v1/auth"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping({"/login", "/auth/login"})
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Authenticate credentials using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Fetch User entity to retrieve ID and assigned Role
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

        // Generate JWT token containing userId, role, and email claims
        String token = jwtUtil.generateToken(user);

        LoginResponse response = new LoginResponse(
                true,
                "Authentication successful",
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                "ROLE_" + user.getRole().name(),
                jwtUtil.getExpirationTime()
        );

        return ResponseEntity.ok(response);
    }
}
