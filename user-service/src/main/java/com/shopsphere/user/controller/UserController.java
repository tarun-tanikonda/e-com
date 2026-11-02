package com.shopsphere.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.user.dto.UserRegisterRequest;
import com.shopsphere.user.dto.UserRegisterResponse;
import com.shopsphere.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping(path = "/register")
	public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
		UserRegisterResponse response = userService.registerUser(userRegisterRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
