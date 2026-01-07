package com.ubs.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.expensemanager.dto.request.ExpenseCategoryCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseCategoryUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseCategoryAuditResponse;
import com.ubs.expensemanager.dto.response.ExpenseCategoryResponse;
import com.ubs.expensemanager.exception.ConflictException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.security.JwtUtil;
import com.ubs.expensemanager.service.ExpenseCategoryService;
import com.ubs.expensemanager.service.UserDetailsServiceImpl;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ExpenseCategoryController}.
 *
 * <p>Tests expense category endpoints including validation,
 * permission handling and business conflicts. Complex business
 * logic is tested in ExpenseCategoryServiceTest.</p>
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ExpenseCategoryController.class)
class ExpenseCategoryControllerTest {

    private static final String CATEGORIES_URL = "/api/expense-categories";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseCategoryService expenseCategoryService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void create_success_returnsCreated() throws Exception {
        ExpenseCategoryCreateRequest request = ExpenseCategoryCreateRequest.builder()
                .name("Food")
                .dailyBudget(new BigDecimal("100.00"))
                .monthlyBudget(new BigDecimal("3000.00"))
                .currencyName("USD")
                .build();

        ExpenseCategoryResponse response = ExpenseCategoryResponse.builder()
                .id(1L)
                .name("Food")
                .dailyBudget(new BigDecimal("100.00"))
                .monthlyBudget(new BigDecimal("3000.00"))
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .build();

        when(expenseCategoryService.create(any())).thenReturn(response);

        mockMvc.perform(post(CATEGORIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/expense-categories/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.dailyBudget").value(100.00))
                .andExpect(jsonPath("$.monthlyBudget").value(3000.00))
                .andExpect(jsonPath("$.currencyName").value("USD"))
                .andExpect(jsonPath("$.exchangeRate").value(1.000000));
    }

    @Test
    void create_invalidRequest_returnsBadRequest() throws Exception {
        ExpenseCategoryCreateRequest request = ExpenseCategoryCreateRequest.builder()
                .name("") // invalid
                .dailyBudget(new BigDecimal("-10.00")) // invalid
                .monthlyBudget(null) // invalid
                .currencyName("") // invalid
                .build();

        mockMvc.perform(post(CATEGORIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.dailyBudget").exists())
                .andExpect(jsonPath("$.errors.monthlyBudget").exists());
    }

    @Test
    void listAll_success_returnsOk() throws Exception {
        List<ExpenseCategoryResponse> categories = List.of(
                ExpenseCategoryResponse.builder()
                        .id(1L)
                        .name("Food")
                        .dailyBudget(new BigDecimal("100.00"))
                        .monthlyBudget(new BigDecimal("3000.00"))
                        .currencyName("USD")
                        .exchangeRate(new BigDecimal("1.000000"))
                        .build(),
                ExpenseCategoryResponse.builder()
                        .id(2L)
                        .name("Transport")
                        .dailyBudget(new BigDecimal("50.00"))
                        .monthlyBudget(new BigDecimal("1500.00"))
                        .currencyName("USD")
                        .exchangeRate(new BigDecimal("1.000000"))
                        .build()
        );

        when(expenseCategoryService.listAll()).thenReturn(categories);

        mockMvc.perform(get(CATEGORIES_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Transport"));
    }

    @Test
    void listAll_noCategories_returnsEmptyList() throws Exception {
        when(expenseCategoryService.listAll()).thenReturn(List.of());

        mockMvc.perform(get(CATEGORIES_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findById_withoutDateTime_returnsOk() throws Exception {
        ExpenseCategoryResponse response = ExpenseCategoryResponse.builder()
                .id(1L)
                .name("Food")
                .dailyBudget(new BigDecimal("100.00"))
                .monthlyBudget(new BigDecimal("3000.00"))
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .build();

        when(expenseCategoryService.findById(eq(1L), eq(null))).thenReturn(response);

        mockMvc.perform(get(CATEGORIES_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.dailyBudget").value(100.00))
                .andExpect(jsonPath("$.monthlyBudget").value(3000.00));
    }

    @Test
    void findById_withDateTime_returnsHistoricalVersion() throws Exception {
        OffsetDateTime dateTime = OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        ExpenseCategoryResponse response = ExpenseCategoryResponse.builder()
                .id(1L)
                .name("Food")
                .dailyBudget(new BigDecimal("80.00"))
                .monthlyBudget(new BigDecimal("2400.00"))
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .build();

        when(expenseCategoryService.findById(eq(1L), any(OffsetDateTime.class)))
                .thenReturn(response);

        mockMvc.perform(get(CATEGORIES_URL + "/1")
                        .param("dateTime", dateTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.dailyBudget").value(80.00))
                .andExpect(jsonPath("$.monthlyBudget").value(2400.00));
    }

    @Test
    void getAuditHistory_success_returnsOk() throws Exception {
        List<ExpenseCategoryAuditResponse> auditHistory = List.of(
                ExpenseCategoryAuditResponse.builder()
                        .id(1L)
                        .name("Food")
                        .dailyBudget(new BigDecimal("100.00"))
                        .monthlyBudget(new BigDecimal("3000.00"))
                        .currencyName("USD")
                        .exchangeRate(new BigDecimal("1.000000"))
                        .revisionNumber(1)
                        .revisionType((short) RevisionType.ADD.ordinal())
                        .revisionDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                        .build(),
                ExpenseCategoryAuditResponse.builder()
                        .id(1L)
                        .name("Food & Beverages")
                        .dailyBudget(new BigDecimal("120.00"))
                        .monthlyBudget(new BigDecimal("3600.00"))
                        .currencyName("USD")
                        .exchangeRate(new BigDecimal("1.000000"))
                        .revisionNumber(2)
                        .revisionType((short) RevisionType.MOD.ordinal())
                        .revisionDate(LocalDateTime.of(2026, 1, 2, 14, 0))
                        .build()
        );

        when(expenseCategoryService.getAuditHistory(1L)).thenReturn(auditHistory);

        mockMvc.perform(get(CATEGORIES_URL + "/1/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].revisionNumber").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[0].dailyBudget").value(100.00))
                .andExpect(jsonPath("$[1].revisionNumber").value(2))
                .andExpect(jsonPath("$[1].name").value("Food & Beverages"))
                .andExpect(jsonPath("$[1].dailyBudget").value(120.00));
    }

    @Test
    void update_success_returnsOk() throws Exception {
        ExpenseCategoryUpdateRequest request = ExpenseCategoryUpdateRequest.builder()
                .name("Food Updated")
                .dailyBudget(new BigDecimal("150.00"))
                .monthlyBudget(new BigDecimal("4500.00"))
                .currencyName("USD")
                .build();

        ExpenseCategoryResponse response = ExpenseCategoryResponse.builder()
                .id(1L)
                .name("Food Updated")
                .dailyBudget(new BigDecimal("150.00"))
                .monthlyBudget(new BigDecimal("4500.00"))
                .currencyName("USD")
                .exchangeRate(new BigDecimal("1.000000"))
                .build();

        when(expenseCategoryService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put(CATEGORIES_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food Updated"))
                .andExpect(jsonPath("$.dailyBudget").value(150.00))
                .andExpect(jsonPath("$.monthlyBudget").value(4500.00));
    }

    @Test
    void update_invalidRequest_returnsBadRequest() throws Exception {
        ExpenseCategoryUpdateRequest request = ExpenseCategoryUpdateRequest.builder()
                .name("") // invalid
                .dailyBudget(new BigDecimal("-10.00")) // invalid
                .monthlyBudget(null) // invalid
                .currencyName("") // invalid
                .build();

        mockMvc.perform(put(CATEGORIES_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.dailyBudget").exists())
                .andExpect(jsonPath("$.errors.monthlyBudget").exists())
                .andExpect(jsonPath("$.errors.currencyName").exists());
    }
}
