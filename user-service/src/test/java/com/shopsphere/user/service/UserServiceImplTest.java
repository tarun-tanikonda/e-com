package com.shopsphere.user.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shopsphere.user.dto.UserRegisterRequest;
import com.shopsphere.user.dto.UserRegisterResponse;
import com.shopsphere.user.dto.request.UserProfileUpdateRequest;
import com.shopsphere.user.dto.response.UserProfileResponse;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.exception.ResourceNotFoundException;
import com.shopsphere.user.exception.UserAlreadyExistsException;
import com.shopsphere.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private UserRepository userRepository;
    private ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        modelMapper = new ModelMapper();
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserServiceImpl(userRepository, modelMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Should successfully register a new user and enforce CUSTOMER role and normalize email")
    void shouldRegisterNewUser() {
        UserRegisterRequest request = new UserRegisterRequest("John", "Doe", "  JOHN.DOE@example.com  ", "Password@123", "1234567890");

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPasswordHash");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setEmail("john.doe@example.com");
        savedUser.setPasswordHash("encodedPasswordHash");
        savedUser.setRole(User.Role.CUSTOMER);
        savedUser.setEnabled(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserRegisterResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("Password@123");
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when registering existing email")
    void shouldThrowWhenEmailAlreadyExists() {
        UserRegisterRequest request = new UserRegisterRequest("John", "Doe", "existing@example.com", "Password@123", null);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(request));
    }

    @Test
    @DisplayName("Should retrieve user profile by ID")
    void shouldGetUserProfileById() {
        User user = new User();
        user.setId(5L);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@example.com");
        user.setRole(User.Role.CUSTOMER);
        user.setEnabled(true);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        UserProfileResponse profile = userService.getUserProfile(5L);

        assertNotNull(profile);
        assertEquals(5L, profile.getId());
        assertEquals("jane.doe@example.com", profile.getEmail());
        assertEquals("Jane", profile.getFirstName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user ID does not exist")
    void shouldThrowWhenUserIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfile(999L));
    }

    @Test
    @DisplayName("Should update user profile fields")
    void shouldUpdateUserProfile() {
        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setFirstName("Old");
        existingUser.setLastName("Name");
        existingUser.setEmail("user@example.com");
        existingUser.setPhoneNumber("1111111111");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest("New", "Updated", "9999999999");
        UserProfileResponse updatedProfile = userService.updateUserProfile(10L, updateRequest);

        assertNotNull(updatedProfile);
        assertEquals("New", updatedProfile.getFirstName());
        assertEquals("Updated", updatedProfile.getLastName());
        assertEquals("9999999999", updatedProfile.getPhoneNumber());
    }

    @Test
    @DisplayName("Should update user status")
    void shouldUpdateUserStatus() {
        User existingUser = new User();
        existingUser.setId(12L);
        existingUser.setEnabled(true);

        when(userRepository.findById(12L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.updateUserStatus(12L, false);

        assertNotNull(response);
        assertEquals(false, response.isEnabled());
    }
}
