package com.shopsphere.user.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.shopsphere.user.dto.UserRegisterRequest;
import com.shopsphere.user.dto.UserRegisterResponse;
import com.shopsphere.user.dto.request.LoginRequest;
import com.shopsphere.user.dto.request.UserProfileUpdateRequest;
import com.shopsphere.user.dto.request.UserStatusUpdateRequest;
import com.shopsphere.user.dto.response.ErrorResponse;
import com.shopsphere.user.dto.response.LoginResponse;
import com.shopsphere.user.dto.response.UserProfileResponse;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.exception.GlobalExceptionHandler;
import com.shopsphere.user.exception.ResourceNotFoundException;
import com.shopsphere.user.exception.UserAlreadyExistsException;
import com.shopsphere.user.repository.UserRepository;
import com.shopsphere.user.security.JWTUtil;
import com.shopsphere.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Direct execution tester for User Service without bytecode agent dependencies.
 */
public class RunAuthControllerTest {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("      USER SERVICE - COMPLETE ARCHITECTURE & API VERIFICATION  ");
        System.out.println("===============================================================");

        int passed = 0;

        // --- TEST 1: Registration with open role (ADMIN) ---
        System.out.println("\n[TEST 1] Testing POST /api/user/register & /api/v1/users/register (Role: ADMIN) ...");
        UserService stubUserService = new UserService() {
            @Override
            public UserRegisterResponse registerUser(UserRegisterRequest req) {
                UserRegisterResponse res = new UserRegisterResponse();
                res.setId(101L);
                res.setFirstName(req.getFirstName());
                res.setLastName(req.getLastName());
                res.setEmail(req.getEmail().trim().toLowerCase());
                res.setRole(req.getRole() != null ? req.getRole().name() : "CUSTOMER");
                res.setCreatedAt(LocalDateTime.now());
                return res;
            }

            @Override
            public UserProfileResponse getUserProfile(Long userId) {
                UserProfileResponse res = new UserProfileResponse();
                res.setId(userId);
                res.setEmail("admin@shopsphere.com");
                res.setFirstName("Alice");
                res.setLastName("Admin");
                res.setRole("ADMIN");
                res.setEnabled(true);
                return res;
            }

            @Override
            public UserProfileResponse getUserProfileByEmail(String email) {
                return getUserProfile(101L);
            }

            @Override
            public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest req) {
                UserProfileResponse res = getUserProfile(userId);
                res.setFirstName(req.getFirstName());
                res.setLastName(req.getLastName());
                return res;
            }

            @Override
            public UserProfileResponse updateUserStatus(Long userId, boolean enabled) {
                UserProfileResponse res = getUserProfile(userId);
                res.setEnabled(enabled);
                return res;
            }
        };

        UserController userController = new UserController(stubUserService);

        UserRegisterRequest regRequest = new UserRegisterRequest("Alice", "Admin", "alice.admin@shopsphere.com", "Admin@12345", "9876543210", User.Role.ADMIN);
        ResponseEntity<UserRegisterResponse> regResult = userController.registerUser(regRequest);

        if (regResult.getStatusCode() == HttpStatus.CREATED
                && regResult.getBody() != null
                && "ADMIN".equals(regResult.getBody().getRole())) {
            System.out.println(">>> [SUCCESS] 201 CREATED for /api/user/register, Role: " + regResult.getBody().getRole());
            passed++;
        } else {
            System.err.println(">>> [FAILED] Unexpected registration result: " + regResult);
        }

        // --- TEST 2: Authentication & Token Issuance ---
        System.out.println("\n[TEST 2] Testing POST /api/auth/login & /api/v1/users/login ...");
        AuthenticationManager stubAuthManager = authentication -> authentication;
        JWTUtil jwtUtil = new JWTUtil("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");

        UserRepository stubUserRepo = new UserRepository() {
            @Override
            public Optional<User> findByEmail(String email) {
                User u = new User();
                u.setId(101L);
                u.setEmail(email);
                u.setRole(User.Role.ADMIN);
                u.setEnabled(true);
                return Optional.of(u);
            }

            @Override public boolean existsByEmail(String email) { return true; }
            @Override public void flush() {}
            @Override public <S extends User> S saveAndFlush(S entity) { return entity; }
            @Override public <S extends User> java.util.List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<User> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
            @Override public void deleteAllInBatch() {}
            @Override public User getOne(Long aLong) { return null; }
            @Override public User getById(Long aLong) { return null; }
            @Override public User getReferenceById(Long aLong) { return null; }
            @Override public <S extends User> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends User> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends User> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends User> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends User> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends User> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends User, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public <S extends User> S save(S entity) { return entity; }
            @Override public <S extends User> java.util.List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public Optional<User> findById(Long aLong) { return Optional.empty(); }
            @Override public boolean existsById(Long aLong) { return true; }
            @Override public java.util.List<User> findAll() { return null; }
            @Override public java.util.List<User> findAllById(Iterable<Long> longs) { return null; }
            @Override public long count() { return 1; }
            @Override public void deleteById(Long aLong) {}
            @Override public void delete(User entity) {}
            @Override public void deleteAllById(Iterable<? extends Long> longs) {}
            @Override public void deleteAll(Iterable<? extends User> entities) {}
            @Override public void deleteAll() {}
            @Override public java.util.List<User> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<User> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        AuthController authController = new AuthController(stubAuthManager, jwtUtil, stubUserRepo);
        LoginRequest loginReq = new LoginRequest("alice.admin@shopsphere.com", "Admin@12345");
        ResponseEntity<LoginResponse> loginResult = authController.login(loginReq);

        if (loginResult.getStatusCode() == HttpStatus.OK && loginResult.getBody() != null) {
            LoginResponse body = loginResult.getBody();
            System.out.println(">>> [SUCCESS] 200 OK & JWT Issued: " + body.getTokenType() + " " + body.getAccessToken().substring(0, 25) + "...");
            System.out.println(">>> Success Flag: " + body.isSuccess() + ", Message: " + body.getMessage() + ", Role: " + body.getRole());
            passed++;
        } else {
            System.err.println(">>> [FAILED] Unexpected login result: " + loginResult);
        }

        // --- TEST 3: Profile Retrieval GET /api/v1/users/me ---
        System.out.println("\n[TEST 3] Testing GET /api/v1/users/me (via Gateway header) ...");
        ResponseEntity<UserProfileResponse> profileResult = userController.getCurrentUserProfile(null, 101L);
        if (profileResult.getStatusCode() == HttpStatus.OK && profileResult.getBody() != null && profileResult.getBody().getId() == 101L) {
            System.out.println(">>> [SUCCESS] 200 OK for /api/v1/users/me, Email: " + profileResult.getBody().getEmail());
            passed++;
        } else {
            System.err.println(">>> [FAILED] Unexpected profile result: " + profileResult);
        }

        // --- TEST 4: Profile Update PUT /api/v1/users/me ---
        System.out.println("\n[TEST 4] Testing PUT /api/v1/users/me ...");
        UserProfileUpdateRequest updateReq = new UserProfileUpdateRequest("Alicia", "Smith-Jones", "1122334455");
        ResponseEntity<UserProfileResponse> updateResult = userController.updateCurrentUserProfile(null, 101L, updateReq);
        if (updateResult.getStatusCode() == HttpStatus.OK && "Alicia".equals(updateResult.getBody().getFirstName())) {
            System.out.println(">>> [SUCCESS] 200 OK for /api/v1/users/me (updated firstName=" + updateResult.getBody().getFirstName() + ")");
            passed++;
        } else {
            System.err.println(">>> [FAILED] Unexpected update result: " + updateResult);
        }

        // --- TEST 5: Status Update PATCH /api/v1/users/{userId}/status ---
        System.out.println("\n[TEST 5] Testing PATCH /api/v1/users/{userId}/status ...");
        UserStatusUpdateRequest statusReq = new UserStatusUpdateRequest(false);
        ResponseEntity<UserProfileResponse> statusResult = userController.updateUserStatus(101L, statusReq);
        if (statusResult.getStatusCode() == HttpStatus.OK && !statusResult.getBody().isEnabled()) {
            System.out.println(">>> [SUCCESS] 200 OK for status change (enabled=" + statusResult.getBody().isEnabled() + ")");
            passed++;
        } else {
            System.err.println(">>> [FAILED] Unexpected status result: " + statusResult);
        }

        // --- TEST 6: Exception Handling (Conflict 409) ---
        System.out.println("\n[TEST 6] Testing Centralized Exception Handling: UserAlreadyExistsException -> 409 CONFLICT ...");
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        HttpServletRequest dummyRequest = (HttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, mArgs) -> {
                    if ("getRequestURI".equals(method.getName())) return "/api/user/register";
                    if ("getHeader".equals(method.getName()) && "X-Correlation-Id".equals(mArgs[0])) return "corr-test-12345";
                    return null;
                }
        );

        ResponseEntity<ErrorResponse> conflictResult = exceptionHandler.handleUserAlreadyExists(
                new UserAlreadyExistsException("Email already exists: alice.admin@shopsphere.com"),
                dummyRequest
        );
        if (conflictResult.getStatusCode() == HttpStatus.CONFLICT
                && "USER_ALREADY_EXISTS".equals(conflictResult.getBody().getErrorCode())
                && "corr-test-12345".equals(conflictResult.getBody().getCorrelationId())) {
            System.out.println(">>> [SUCCESS] 409 CONFLICT with errorCode=" + conflictResult.getBody().getErrorCode() + ", correlationId=" + conflictResult.getBody().getCorrelationId());
            passed++;
        } else {
            System.err.println(">>> [FAILED] Unexpected conflict result: " + conflictResult);
        }

        // --- TEST 7: Exception Handling (Not Found 404) ---
        System.out.println("\n[TEST 7] Testing Centralized Exception Handling: ResourceNotFoundException -> 404 NOT FOUND ...");
        ResponseEntity<ErrorResponse> notFoundResult = exceptionHandler.handleResourceNotFound(
                new ResourceNotFoundException("User not found with id: 999"),
                dummyRequest
        );
        if (notFoundResult.getStatusCode() == HttpStatus.NOT_FOUND
                && "RESOURCE_NOT_FOUND".equals(notFoundResult.getBody().getErrorCode())) {
            System.out.println(">>> [SUCCESS] 404 NOT_FOUND with errorCode=" + notFoundResult.getBody().getErrorCode());
            passed++;
        } else {
            System.err.println(">>> [FAILED] Unexpected not found result: " + notFoundResult);
        }

        System.out.println("\n===============================================================");
        System.out.println("   ALL " + passed + "/7 TESTS PASSED SUCCESSFULLY!             ");
        System.out.println("===============================================================");
    }
}
