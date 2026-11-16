package com.shopsphere.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.user.dto.request.UserRegisterRequest;
import com.shopsphere.user.dto.response.UserRegisterResponse;
import com.shopsphere.user.dto.request.UserProfileUpdateRequest;
import com.shopsphere.user.dto.request.UserStatusUpdateRequest;
import com.shopsphere.user.dto.response.UserProfileResponse;
import com.shopsphere.user.service.UserService;

import jakarta.validation.Valid;

/**
 * REST controller for user registration and profile management.
 * Supports both SRS paths (/api/user/register) and versioned gateway paths (/api/v1/users/register).
 */
@RestController
@RequestMapping({"/api/v1/users", "/api/user", "/api/v1/user"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping({"/register", "/auth/register"})
    public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        UserRegisterResponse response = userService.registerUser(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable("userId") Long userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) Long gatewayUserId) {

        UserProfileResponse response;
        if (gatewayUserId != null) {
            response = userService.getUserProfile(gatewayUserId);
        } else if (authentication != null && authentication.getName() != null) {
            response = userService.getUserProfileByEmail(authentication.getName());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) Long gatewayUserId,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        Long userId = gatewayUserId;
        if (userId == null && authentication != null && authentication.getName() != null) {
            UserProfileResponse currentProfile = userService.getUserProfileByEmail(authentication.getName());
            userId = currentProfile.getId();
        }
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateUserStatus(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        UserProfileResponse response = userService.updateUserStatus(userId, request.getEnabled());
        return ResponseEntity.ok(response);
    }
}
