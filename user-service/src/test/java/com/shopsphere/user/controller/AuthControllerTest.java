package com.shopsphere.user.controller;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.shopsphere.user.dto.request.UserRegisterRequest;
import com.shopsphere.user.dto.response.UserRegisterResponse;
import com.shopsphere.user.dto.request.LoginRequest;
import com.shopsphere.user.dto.response.LoginResponse;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.repository.UserRepository;
import com.shopsphere.user.security.JWTUtil;
import com.shopsphere.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private AuthenticationManager authenticationManager;
    private JWTUtil jwtUtil;
    private UserRepository userRepository;
    private AuthController authController;

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtil = mock(JWTUtil.class);
        userRepository = mock(UserRepository.class);
        authController = new AuthController(authenticationManager, jwtUtil, userRepository);

        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    @DisplayName("POST /api/v1/users/login should authenticate and return 200 OK with token details")
    void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest("john.doe@shopsphere.com", "Password@123");

        User user = new User();
        user.setId(101L);
        user.setEmail("john.doe@shopsphere.com");
        user.setRole(User.Role.CUSTOMER);

        Authentication authResult = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResult);
        when(userRepository.findByEmail("john.doe@shopsphere.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("mock.jwt.token");
        when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mock.jwt.token", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(101L, response.getBody().getUserId());
        assertEquals("ROLE_CUSTOMER", response.getBody().getRole());
        assertEquals("john.doe@shopsphere.com", response.getBody().getEmail());
    }

    @Test
    @DisplayName("POST /api/v1/users/register should create user and return 201 CREATED")
    void testRegisterSuccess() {
        UserRegisterRequest regRequest = new UserRegisterRequest();
        regRequest.setFirstName("John");
        regRequest.setLastName("Doe");
        regRequest.setEmail("john.doe@shopsphere.com");
        regRequest.setPassword("Password@123");
        regRequest.setRole(User.Role.CUSTOMER);

        UserRegisterResponse mockResponse = new UserRegisterResponse();
        mockResponse.setId(101L);
        mockResponse.setFirstName("John");
        mockResponse.setLastName("Doe");
        mockResponse.setEmail("john.doe@shopsphere.com");

        when(userService.registerUser(any(UserRegisterRequest.class))).thenReturn(mockResponse);

        ResponseEntity<UserRegisterResponse> response = userController.registerUser(regRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().getId());
        assertEquals("john.doe@shopsphere.com", response.getBody().getEmail());

        verify(userService).registerUser(regRequest);
    }
}
