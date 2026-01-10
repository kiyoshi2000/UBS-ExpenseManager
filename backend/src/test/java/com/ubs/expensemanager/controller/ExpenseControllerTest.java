package com.ubs.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.expensemanager.dto.request.ExpenseCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseFilterRequest;
import com.ubs.expensemanager.dto.request.ExpenseUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseResponse;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.security.JwtUtil;
import com.ubs.expensemanager.service.ExpenseService;
import com.ubs.expensemanager.service.UserDetailsServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ExpenseController}.
 *
 * <p>Tests expense management endpoints using {@link MockMvc}.
 * {@link ExpenseService} is mocked to isolate controller behavior.</p>
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    private static final String BASE_URL = "/api/expenses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private ExpenseResponse expenseResponse;
    private ExpenseCreateRequest createRequest;
    private ExpenseUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        expenseResponse = ExpenseResponse.builder()
                .id(1L)
                .amount(new BigDecimal("150.50"))
                .description("Team lunch at restaurant")
                .expenseDate(LocalDate.of(2026, 1, 8))
                .userId(1L)
                .userName("John Doe")
                .userEmail("john.doe@ubs.com")
                .expenseCategoryId(1L)
                .expenseCategoryName("Food")
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .receiptUrl("https://example.com/receipts/12345.pdf")
                .status(ExpenseStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = ExpenseCreateRequest.builder()
                .amount(new BigDecimal("150.50"))
                .description("Team lunch at restaurant")
                .expenseDate(LocalDate.of(2026, 1, 8))
                .expenseCategoryId(1L)
                .currencyName("USD")
                .receiptUrl("https://example.com/receipts/12345.pdf")
                .build();

        updateRequest = ExpenseUpdateRequest.builder()
                .amount(new BigDecimal("200.00"))
                .description("Updated lunch")
                .expenseDate(LocalDate.of(2026, 1, 8))
                .expenseCategoryId(1L)
                .currencyName("USD")
                .receiptUrl("https://example.com/receipts/updated.pdf")
                .build();
    }

    @Test
    void create_success_returnsCreated() throws Exception {
        when(expenseService.create(any(ExpenseCreateRequest.class))).thenReturn(expenseResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/expenses/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(150.50))
                .andExpect(jsonPath("$.description").value("Team lunch at restaurant"))
                .andExpect(jsonPath("$.expenseDate").value("2026-01-08"))
                .andExpect(jsonPath("$.expenseCategoryId").value(1))
                .andExpect(jsonPath("$.currencyName").value("USD"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(expenseService).create(any(ExpenseCreateRequest.class));
    }

    @Test
    void create_invalidRequest_returnsBadRequest() throws Exception {
        ExpenseCreateRequest invalidRequest = ExpenseCreateRequest.builder()
                .amount(new BigDecimal("-10.00")) // negative amount
                .description(null)
                .expenseDate(null) // required
                .expenseCategoryId(null) // required
                .currencyName("") // blank
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"));

        verify(expenseService, never()).create(any(ExpenseCreateRequest.class));
    }

    @Test
    void findAll_success_returnsOk() throws Exception {
        ExpenseResponse expense2 = ExpenseResponse.builder()
                .id(2L)
                .amount(new BigDecimal("75.00"))
                .description("Office supplies")
                .expenseDate(LocalDate.of(2026, 1, 7))
                .userId(1L)
                .userName("John Doe")
                .userEmail("john.doe@ubs.com")
                .expenseCategoryId(2L)
                .expenseCategoryName("Office")
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .status(ExpenseStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page<ExpenseResponse> page = new PageImpl<>(
                List.of(expenseResponse, expense2),
                PageRequest.of(0, 10),
                2
        );

        when(expenseService.findAll(any(ExpenseFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(expenseService).findAll(any(ExpenseFilterRequest.class), any(Pageable.class));
    }

    @Test
    void findById_success_returnsOk() throws Exception {
        when(expenseService.findById(1L)).thenReturn(expenseResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(150.50))
                .andExpect(jsonPath("$.description").value("Team lunch at restaurant"));

        verify(expenseService).findById(1L);
    }

    @Test
    void update_success_returnsOk() throws Exception {
        ExpenseResponse updatedResponse = ExpenseResponse.builder()
                .id(1L)
                .amount(new BigDecimal("200.00"))
                .description("Updated lunch")
                .expenseDate(LocalDate.of(2026, 1, 8))
                .userId(1L)
                .userName("John Doe")
                .userEmail("john.doe@ubs.com")
                .expenseCategoryId(1L)
                .expenseCategoryName("Food")
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .receiptUrl("https://example.com/receipts/updated.pdf")
                .status(ExpenseStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(expenseService.update(eq(1L), any(ExpenseUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.description").value("Updated lunch"))
                .andExpect(jsonPath("$.receiptUrl").value("https://example.com/receipts/updated.pdf"));

        verify(expenseService).update(eq(1L), any(ExpenseUpdateRequest.class));
    }

    @Test
    void update_invalidRequest_returnsBadRequest() throws Exception {
        ExpenseUpdateRequest invalidRequest = ExpenseUpdateRequest.builder()
                .amount(null) // required
                .description(null)
                .expenseDate(null) // required
                .expenseCategoryId(null) // required
                .currencyName("") // blank
                .build();

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"));

        verify(expenseService, never()).update(eq(1L), any(ExpenseUpdateRequest.class));
    }

    @Test
    void approve_success_returnsOk() throws Exception {
        ExpenseResponse approvedResponse = ExpenseResponse.builder()
                .id(1L)
                .amount(new BigDecimal("150.50"))
                .description("Team lunch at restaurant")
                .expenseDate(LocalDate.of(2026, 1, 8))
                .userId(1L)
                .userName("John Doe")
                .userEmail("john.doe@ubs.com")
                .expenseCategoryId(1L)
                .expenseCategoryName("Food")
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .status(ExpenseStatus.APPROVED_BY_MANAGER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(expenseService.approve(1L)).thenReturn(approvedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/approve", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED_BY_MANAGER"));

        verify(expenseService).approve(1L);
    }

    @Test
    void reject_success_returnsOk() throws Exception {
        ExpenseResponse rejectedResponse = ExpenseResponse.builder()
                .id(1L)
                .amount(new BigDecimal("150.50"))
                .description("Team lunch at restaurant")
                .expenseDate(LocalDate.of(2026, 1, 8))
                .userId(1L)
                .userName("John Doe")
                .userEmail("john.doe@ubs.com")
                .expenseCategoryId(1L)
                .expenseCategoryName("Food")
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .status(ExpenseStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(expenseService.reject(1L)).thenReturn(rejectedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/reject", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(expenseService).reject(1L);
    }

    @Test
    void requestRevision_success_returnsOk() throws Exception {
        ExpenseResponse revisionResponse = ExpenseResponse.builder()
                .id(1L)
                .amount(new BigDecimal("150.50"))
                .description("Team lunch at restaurant")
                .expenseDate(LocalDate.of(2026, 1, 8))
                .userId(1L)
                .userName("John Doe")
                .userEmail("john.doe@ubs.com")
                .expenseCategoryId(1L)
                .expenseCategoryName("Food")
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .status(ExpenseStatus.REQUIRES_REVISION)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(expenseService.requestRevision(1L)).thenReturn(revisionResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/request-revision", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REQUIRES_REVISION"));

        verify(expenseService).requestRevision(1L);
    }

    @Test
    void delete_success_returnsNoContent() throws Exception {
        doNothing().when(expenseService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(expenseService).delete(1L);
    }

    @Test
    void findAll_withStatusFilter_returnsFilteredExpenses() throws Exception {
        Page<ExpenseResponse> page = new PageImpl<>(
                List.of(expenseResponse),
                PageRequest.of(0, 10),
                1
        );

        when(expenseService.findAll(any(ExpenseFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        verify(expenseService).findAll(any(ExpenseFilterRequest.class), any(Pageable.class));
    }

    @Test
    void findAll_withDateFilter_returnsFilteredExpenses() throws Exception {
        Page<ExpenseResponse> page = new PageImpl<>(
                List.of(expenseResponse),
                PageRequest.of(0, 10),
                1
        );

        when(expenseService.findAll(any(ExpenseFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(expenseService).findAll(any(ExpenseFilterRequest.class), any(Pageable.class));
    }

    @Test
    void findAll_noExpenses_returnsEmptyList() throws Exception {
        Page<ExpenseResponse> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(expenseService.findAll(any(ExpenseFilterRequest.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(expenseService).findAll(any(ExpenseFilterRequest.class), any(Pageable.class));
    }
}
