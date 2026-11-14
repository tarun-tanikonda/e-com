package com.shopsphere.user.dto.response;

/**
 * Standard response DTO for user authentication per ShopSphere SRS Section 6.2.
 * Returns success flag, message, signed JWT access token, token type, expiry duration, and user details.
 */
public class LoginResponse {

    private boolean success;
    private String message;
    private String accessToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String role;
    private Long expiresIn;

    public LoginResponse() {
        this.success = true;
        this.message = "Authentication successful";
        this.tokenType = "Bearer";
    }

    public LoginResponse(String accessToken, String tokenType, Long userId, String email, String role, Long expiresIn) {
        this.success = true;
        this.message = "Authentication successful";
        this.accessToken = accessToken;
        this.tokenType = (tokenType != null) ? tokenType : "Bearer";
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public LoginResponse(boolean success, String message, String accessToken, String tokenType, Long userId, String email, String role, Long expiresIn) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.tokenType = (tokenType != null) ? tokenType : "Bearer";
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
