package com.ubs.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.security.JwtUtil;
import com.ubs.expensemanager.service.AuthService;
import com.ubs.expensemanager.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AuthController}.
 *
 * <p>Tests authentication endpoints including login validation and error handling.</p>
 *
 * <p>Uses {@link MockMvc} to simulate HTTP requests and verify responses.</p>
 * and {@link AuthService} is mocked to isolate controller behaviour
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private static final String LOGIN_URL = "/api/auth/login";

    /**
     * Verifies that an invalid login attempt returns HTTP 401 Unauthorized.
     *
     * @throws Exception if the request execution fails
     */
    @Test
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("inexistent_user@x.x")
                .password("password")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid Credentials"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    /**
     * Verifies that a request with invalid fields returns HTTP 400 Bad Request
     * with detailed validation error messages (invalid email and empty password).
     *
     * @throws Exception if the request execution fails
     */
    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("invalid_email_address")
                .password("")
                .build();

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }
}

