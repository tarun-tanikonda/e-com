package com.shopsphere.user.service;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.user.dto.request.UserRegisterRequest;
import com.shopsphere.user.dto.response.UserRegisterResponse;
import com.shopsphere.user.dto.request.UserProfileUpdateRequest;
import com.shopsphere.user.dto.response.UserProfileResponse;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.exception.ResourceNotFoundException;
import com.shopsphere.user.exception.UserAlreadyExistsException;
import com.shopsphere.user.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest) {
        String normalizedEmail = userRegisterRequest.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Email already exists: " + normalizedEmail);
        }

        User user = modelMapper.map(userRegisterRequest, User.class);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(userRegisterRequest.getPassword()));

        // Open ADMIN / CUSTOMER registration policy per SRS Section 3.3
        User.Role assignedRole = (userRegisterRequest.getRole() != null)
                ? userRegisterRequest.getRole()
                : User.Role.CUSTOMER;
        user.setRole(assignedRole);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserRegisterResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return modelMapper.map(user, UserProfileResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfileByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));
        return modelMapper.map(user, UserProfileResponse.class);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserProfileResponse.class);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserProfileResponse.class);
    }
}
