package com.shopsphere.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shopsphere.user.dto.UserRegisterRequest;
import com.shopsphere.user.dto.UserRegisterResponse;
import com.shopsphere.user.dto.request.UserProfileUpdateRequest;
import com.shopsphere.user.dto.request.UserStatusUpdateRequest;
import com.shopsphere.user.dto.response.UserProfileResponse;
import com.shopsphere.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    @DisplayName("GET /api/v1/users/me with gateway header should return user profile")
    void shouldGetCurrentUserProfileWithGatewayHeader() {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(10L);
        profile.setEmail("user@shopsphere.com");
        profile.setFirstName("User");
        profile.setLastName("Test");

        when(userService.getUserProfile(10L)).thenReturn(profile);

        ResponseEntity<UserProfileResponse> response = userController.getCurrentUserProfile(null, 10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("user@shopsphere.com", response.getBody().getEmail());
    }

    @Test
    @DisplayName("GET /api/v1/users/me with Authentication should return user profile by email")
    void shouldGetCurrentUserProfileWithAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth.user@shopsphere.com");

        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(20L);
        profile.setEmail("auth.user@shopsphere.com");

        when(userService.getUserProfileByEmail("auth.user@shopsphere.com")).thenReturn(profile);

        ResponseEntity<UserProfileResponse> response = userController.getCurrentUserProfile(auth, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("auth.user@shopsphere.com", response.getBody().getEmail());
    }

    @Test
    @DisplayName("PUT /api/v1/users/me should update current user profile")
    void shouldUpdateCurrentUserProfile() {
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest("UpdatedFirst", "UpdatedLast", "9876543210");
        UserProfileResponse updatedProfile = new UserProfileResponse();
        updatedProfile.setId(10L);
        updatedProfile.setFirstName("UpdatedFirst");
        updatedProfile.setLastName("UpdatedLast");
        updatedProfile.setPhoneNumber("9876543210");

        when(userService.updateUserProfile(eq(10L), any(UserProfileUpdateRequest.class))).thenReturn(updatedProfile);

        ResponseEntity<UserProfileResponse> response = userController.updateCurrentUserProfile(null, 10L, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UpdatedFirst", response.getBody().getFirstName());
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} should return profile for admin")
    void shouldGetUserById() {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(15L);
        profile.setEmail("target@shopsphere.com");

        when(userService.getUserProfile(15L)).thenReturn(profile);

        ResponseEntity<UserProfileResponse> response = userController.getUserById(15L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(15L, response.getBody().getId());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{userId}/status should update user status")
    void shouldUpdateUserStatus() {
        UserStatusUpdateRequest statusRequest = new UserStatusUpdateRequest(false);
        UserProfileResponse updatedProfile = new UserProfileResponse();
        updatedProfile.setId(15L);
        updatedProfile.setEnabled(false);

        when(userService.updateUserStatus(15L, false)).thenReturn(updatedProfile);

        ResponseEntity<UserProfileResponse> response = userController.updateUserStatus(15L, statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody().isEnabled());
    }
}
