package com.ubs.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.expensemanager.dto.request.DepartmentCreateRequest;
import com.ubs.expensemanager.exception.ConflictException;
import com.ubs.expensemanager.security.JwtUtil;
import com.ubs.expensemanager.service.DepartmentService;
import com.ubs.expensemanager.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link DepartmentController}.
 *
 * <p>Tests department creation logic including validation,
 * permission handling and business conflicts.</p>
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    private static final String DEPARTMENTS_URL = "/api/departments";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Verifies that a valid department creation request
     * returns HTTP 201 Created.
     */
    @Test
    void shouldCreateDepartmentSuccessfully() throws Exception {
        DepartmentCreateRequest request = DepartmentCreateRequest.builder()
                .name("Finance")
                .monthlyBudget(BigDecimal.valueOf(3000))
                .currency("USD")
                .build();

        when(departmentService.create(any()))
                .thenReturn(
                    DepartmentResponse.builder()
                        .id(1L)
                        .name("Finance")
                        .monthlyBudget(BigDecimal.valueOf(3000))
                        .currency("USD")
                        .build()
                );

        mockMvc.perform(post(DEPARTMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/departments/1"));
    }


    /**
     * Verifies that invalid input returns HTTP 400 Bad Request.
     */
    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        DepartmentCreateRequest request = DepartmentCreateRequest.builder()
                .name("") // invalid
                .monthlyBudget(BigDecimal.valueOf(-10)) // invalid
                .currency("") // invalid
                .build();

        mockMvc.perform(post(DEPARTMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.monthlyBudget").exists())
                .andExpect(jsonPath("$.errors.currency").exists());
    }

    /**
     * Verifies that creating a department with an existing name
     * returns HTTP 409 Conflict.
     */
    @Test
    void shouldReturn409WhenDepartmentAlreadyExists() throws Exception {
        DepartmentCreateRequest request = DepartmentCreateRequest.builder()
                .name("Finance")
                .monthlyBudget(BigDecimal.valueOf(3000))
                .currency("USD")
                .build();

        when(departmentService.create(any()))
                .thenThrow(new ConflictException("Department already exists"));

        mockMvc.perform(post(DEPARTMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    /**
     * Verifies that access denied results in HTTP 403 Forbidden.
     */
    @Test
    void shouldReturn403WhenUserHasNoPermission() throws Exception {
        DepartmentCreateRequest request = DepartmentCreateRequest.builder()
                .name("Finance")
                .monthlyBudget(BigDecimal.valueOf(3000))
                .currency("USD")
                .build();

        when(departmentService.create(any()))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(post(DEPARTMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
