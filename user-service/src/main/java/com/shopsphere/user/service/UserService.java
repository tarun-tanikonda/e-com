package com.shopsphere.user.service;

import com.shopsphere.user.dto.UserRegisterRequest;
import com.shopsphere.user.dto.UserRegisterResponse;

public interface UserService {

	public UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest);

}
