package com.shopsphere.user.service;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopsphere.user.dto.UserRegisterRequest;
import com.shopsphere.user.dto.UserRegisterResponse;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(
			UserRepository userRepository,
			ModelMapper modelMapper,
			PasswordEncoder passwordEncoder)
			{

		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest) {
		try {
			String email = userRegisterRequest.getEmail();
			if (userRepository.existsByEmail(email)) {
				throw new IllegalArgumentException("Email already exists");
			}
			User user = modelMapper.map(userRegisterRequest, User.class);
			user.setPasswordHash(passwordEncoder.encode(userRegisterRequest.getPassword()));
			user.setRole(userRegisterRequest.getRole());
			User savedUser = userRepository.save(user);
			return modelMapper.map(savedUser, UserRegisterResponse.class);
		} catch (Exception e) {
			throw e;
		}
	}
}