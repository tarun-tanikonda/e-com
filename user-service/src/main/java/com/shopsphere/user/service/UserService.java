package com.shopsphere.user.service;

import com.shopsphere.user.dto.request.UserRegisterRequest;
import com.shopsphere.user.dto.response.UserRegisterResponse;
import com.shopsphere.user.dto.request.UserProfileUpdateRequest;
import com.shopsphere.user.dto.response.UserProfileResponse;

public interface UserService {

    UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest);

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse getUserProfileByEmail(String email);

    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);

    UserProfileResponse updateUserStatus(Long userId, boolean enabled);
}
