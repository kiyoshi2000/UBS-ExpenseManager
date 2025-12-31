package com.ubs.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.expensemanager.dto.request.UserFilterRequest;
import com.ubs.expensemanager.dto.request.UserUpdateRequest;
import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.security.JwtAuthFilter;
import com.ubs.expensemanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link UserController}.
 *
 * <p>Tests User management endpoints using {@link MockMvc}.
 * {@link UserService} is mocked to isolate controller behavior.</p>
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private UserResponse managerResponse;
    private UserResponse employeeResponse;

    private static final String BASE_URL = "/api/users";

    /**
     * Initializes reusable test data.
     */
    @BeforeEach
    void setUp() {
        managerResponse = UserResponse.builder()
                .id(1L)
                .email("manager@ubs.com")
                .name("Manager")
                .role("ROLE_MANAGER")
                .build();

        employeeResponse = UserResponse.builder()
                .id(2L)
                .email("employee@ubs.com")
                .name("Employee")
                .role("ROLE_EMPLOYEE")
                .manager(
                        UserResponse.ManagerInfo.builder()
                                .id(1L)
                                .name("Manager")
                                .email("manager@ubs.com")
                                .build()
                )
                .build();
    }

    /**
     * Verifies that all users are returned successfully.
     */
    @Test
    void shouldReturnAllUsers() throws Exception {
        UserFilterRequest filters = new UserFilterRequest();

        Page<UserResponse> page = new PageImpl<>(
                List.of(managerResponse, employeeResponse),
                PageRequest.of(0, 20),
                2
        );

        when(userService.findAll(any(UserFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(userService).findAll(any(UserFilterRequest.class), any(Pageable.class));
    }

    /**
     * Verifies that a user is returned when a valid ID is provided.
     */
    @Test
    void shouldReturnUserById() throws Exception {
        when(userService.findById(2L)).thenReturn(employeeResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("employee@ubs.com"));
    }

    /**
     * Verifies that updating a user returns the updated data.
     */
    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("employee@ubs.com")
                .password("123456")
                .role(UserRole.EMPLOYEE)
                .name("Employee Updated")
                .managerEmail("manager@ubs.com")
                .build();

        UserResponse updated = UserResponse.builder()
                .id(2L)
                .email("employee@ubs.com")
                .name("Employee Updated")
                .role("ROLE_EMPLOYEE")
                .manager(
                        UserResponse.ManagerInfo.builder()
                                .id(1L)
                                .name("Manager")
                                .email("manager@ubs.com")
                                .build()
                )
                .build();

        when(userService.update(eq(2L), any(UserUpdateRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Employee Updated"))
                .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"))
                .andExpect(jsonPath("$.email").value("employee@ubs.com"))
                .andExpect(jsonPath("$.manager.email").value("manager@ubs.com"));
    }


    @Test
    void shouldDeactivateUserSuccessfully() throws Exception {
        doNothing().when(userService).deactivate(2L);

        mockMvc.perform(delete(BASE_URL + "/{id}", 2L))
                .andExpect(status().isNoContent());

        verify(userService).deactivate(2L);
    }
}