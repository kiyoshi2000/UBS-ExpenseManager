package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.request.UserCreateRequest;
import com.ubs.expensemanager.dto.response.LoginResponse;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>Tests authentication and registration logic in isolation, mocking
 * {@link AuthenticationManager}, {@link UserService} and {@link JwtUtil}.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User manager;
    private User employee;
    private UserCreateRequest employeeCreateRequest;
    private LoginRequest loginRequest;

    /**
     * Initializes reusable test data.
     */
    @BeforeEach
    void setUp() {
        manager = User.builder()
                .id(1L)
                .email("manager@ubs.com")
                .role(UserRole.MANAGER)
                .name("Manager")
                .build();

        employee = User.builder()
                .id(2L)
                .email("employee@ubs.com")
                .role(UserRole.EMPLOYEE)
                .name("Employee")
                .manager(manager)
                .build();

        employeeCreateRequest = UserCreateRequest.builder()
                .email("employee@ubs.com")
                .password("password")
                .name("Employee")
                .role(UserRole.EMPLOYEE)
                .managerEmail("manager@ubs.com")
                .build();

        loginRequest = LoginRequest.builder()
                .email("employee@ubs.com")
                .password("password")
                .build();
    }

    /**
     * Verifies that registering a valid EMPLOYEE with manager returns a JWT token.
     */
    @Test
    void shouldRegisterEmployeeWithManagerAndReturnToken() {
        when(userService.createUser(employeeCreateRequest)).thenReturn(employee);
        when(jwtUtil.generateToken(employee)).thenReturn("jwt-token");

        LoginResponse response = authService.register(employeeCreateRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("employee@ubs.com", response.getUser().getEmail());
    }

    /**
     * Verifies that valid credentials authenticate successfully and return a JWT token.
     */
    @Test
    void shouldLoginSuccessfully() {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(employee);
        when(jwtUtil.generateToken(employee)).thenReturn("jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("employee@ubs.com", response.getUser().getEmail());
    }

    /**
     * Verifies that invalid credentials propagate exception.
     */
    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
    }
}

