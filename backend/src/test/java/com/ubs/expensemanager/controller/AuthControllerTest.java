package com.ubs.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.exception.InvalidCredentialsException;
import com.ubs.expensemanager.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private static final String LOGIN_URL = "/api/auth/login";

    // ---------- 401 ----------
    @Test
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request =
                new LoginRequest("inexistent_user_email@email.com", "password");

        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid Credentials"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ---------- 400 ----------
    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("invalid_email_address", "");

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

