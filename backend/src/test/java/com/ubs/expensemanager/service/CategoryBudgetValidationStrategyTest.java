package com.ubs.expensemanager.service;

import com.ubs.expensemanager.event.BudgetExceededEvent;
import com.ubs.expensemanager.event.EventPublisher;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.ExpenseRepository;
import com.ubs.expensemanager.service.budget.CategoryBudgetValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryBudgetValidationStrategyTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CategoryBudgetValidationStrategy strategy;

    @Captor
    private ArgumentCaptor<BudgetExceededEvent> eventCaptor;

    private User employee;
    private Department itDepartment;
    private ExpenseCategory foodCategory;
    private Currency usdCurrency;
    private Expense expense;

    @BeforeEach
    void setUp() {
        // Setup Department
        itDepartment = Department.builder()
                .id(1L)
                .name("IT")
                .dailyBudget(BigDecimal.valueOf(400))
                .monthlyBudget(BigDecimal.valueOf(12000))
                .build();

        // Setup User
        employee = User.builder()
                .id(1L)
                .name("John Employee")
                .email("employee@ubs.com")
                .role(UserRole.EMPLOYEE)
                .department(itDepartment)
                .active(true)
                .build();

        // Setup Currency
        usdCurrency = Currency.builder()
                .id(1L)
                .name("USD")
                .exchangeRate(BigDecimal.ONE)
                .build();

        // Setup ExpenseCategory
        foodCategory = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .dailyBudget(BigDecimal.valueOf(100))
                .monthlyBudget(BigDecimal.valueOf(3000))
                .currency(usdCurrency)
                .build();

        // Setup Expense
        expense = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(50))
                .description("Team lunch")
                .expenseDate(LocalDate.now())
                .user(employee)
                .expenseCategory(foodCategory)
                .currency(usdCurrency)
                .status(ExpenseStatus.PENDING)
                .build();
    }

    @Test
    void validate_dailyBudgetNotExceeded_noEventPublished() {
        // Given
        BigDecimal currentDailyTotal = BigDecimal.valueOf(40);
        BigDecimal newAmount = BigDecimal.valueOf(50);
        // 40 + 50 = 90, which is less than the daily budget of 100

        when(expenseRepository.sumAmountByUserAndCategoryAndDateExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), eq(expense.getId())))
                .thenReturn(currentDailyTotal);

        // When
        strategy.validate(employee.getId(), foodCategory, expense, newAmount);

        // Then
        verify(expenseRepository).sumAmountByUserAndCategoryAndDateExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), eq(expense.getId()));
        verify(expenseRepository).sumAmountByUserAndCategoryAndDateRangeExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), any(LocalDate.class), eq(expense.getId()));
        verify(eventPublisher, never()).publishBudgetExceededEvent(any());
    }

    @Test
    void validate_dailyBudgetExceeded_eventPublished() {
        // Given
        BigDecimal currentDailyTotal = BigDecimal.valueOf(60);
        BigDecimal newAmount = BigDecimal.valueOf(50);
        // 60 + 50 = 110, which exceeds the daily budget of 100

        when(expenseRepository.sumAmountByUserAndCategoryAndDateExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), eq(expense.getId())))
                .thenReturn(currentDailyTotal);

        // When
        strategy.validate(employee.getId(), foodCategory, expense, newAmount);

        // Then
        verify(expenseRepository).sumAmountByUserAndCategoryAndDateExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), eq(expense.getId()));
        verify(eventPublisher).publishBudgetExceededEvent(eventCaptor.capture());

        BudgetExceededEvent capturedEvent = eventCaptor.getValue();
        assertEquals(BudgetExceededEvent.BudgetType.CATEGORY, capturedEvent.getBudgetType());
        assertEquals(expense, capturedEvent.getExpense());
        assertEquals(foodCategory, capturedEvent.getCategory());
        assertEquals(employee.getId(), capturedEvent.getUserId());
        assertEquals(currentDailyTotal, capturedEvent.getCurrentTotal());
        assertEquals(currentDailyTotal.add(newAmount), capturedEvent.getNewTotal());
        assertEquals(foodCategory.getDailyBudget(), capturedEvent.getBudgetLimit());
        assertEquals(expense.getExpenseDate(), capturedEvent.getDate());
    }

    @Test
    void validate_monthlyBudgetExceeded_eventPublished() {
        // Given
        BigDecimal currentDailyTotal = BigDecimal.valueOf(40);
        BigDecimal currentMonthlyTotal = BigDecimal.valueOf(2980);
        BigDecimal newAmount = BigDecimal.valueOf(50);
        // 2980 + 50 = 3030, which exceeds the monthly budget of 3000

        when(expenseRepository.sumAmountByUserAndCategoryAndDateExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), eq(expense.getId())))
                .thenReturn(currentDailyTotal);
        when(expenseRepository.sumAmountByUserAndCategoryAndDateRangeExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), any(LocalDate.class), eq(expense.getId())))
                .thenReturn(currentMonthlyTotal);

        // When
        strategy.validate(employee.getId(), foodCategory, expense, newAmount);

        // Then
        verify(expenseRepository).sumAmountByUserAndCategoryAndDateExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), eq(expense.getId()));
        verify(expenseRepository).sumAmountByUserAndCategoryAndDateRangeExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), any(LocalDate.class), eq(expense.getId()));
        verify(eventPublisher).publishBudgetExceededEvent(eventCaptor.capture());

        BudgetExceededEvent capturedEvent = eventCaptor.getValue();
        assertEquals(BudgetExceededEvent.BudgetType.CATEGORY, capturedEvent.getBudgetType());
        assertEquals(expense, capturedEvent.getExpense());
        assertEquals(foodCategory, capturedEvent.getCategory());
        assertEquals(employee.getId(), capturedEvent.getUserId());
        assertEquals(currentMonthlyTotal, capturedEvent.getCurrentTotal());
        assertEquals(currentMonthlyTotal.add(newAmount), capturedEvent.getNewTotal());
        assertEquals(foodCategory.getMonthlyBudget(), capturedEvent.getBudgetLimit());
        assertNotNull(capturedEvent.getYearMonth());
        assertEquals(YearMonth.from(expense.getExpenseDate()), capturedEvent.getYearMonth());
    }

    @Test
    void validate_bothBudgetsExceeded_twoEventsPublished() {
        // Given
        BigDecimal currentDailyTotal = BigDecimal.valueOf(60);
        BigDecimal currentMonthlyTotal = BigDecimal.valueOf(2980);
        BigDecimal newAmount = BigDecimal.valueOf(50);
        // Daily: 60 + 50 = 110, which exceeds the daily budget of 100
        // Monthly: 2980 + 50 = 3030, which exceeds the monthly budget of 3000

        when(expenseRepository.sumAmountByUserAndCategoryAndDateExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), eq(expense.getId())))
                .thenReturn(currentDailyTotal);
        when(expenseRepository.sumAmountByUserAndCategoryAndDateRangeExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), any(LocalDate.class), eq(expense.getId())))
                .thenReturn(currentMonthlyTotal);

        // When
        strategy.validate(employee.getId(), foodCategory, expense, newAmount);

        // Then
        verify(expenseRepository).sumAmountByUserAndCategoryAndDateExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), eq(expense.getId()));
        verify(expenseRepository).sumAmountByUserAndCategoryAndDateRangeExcludingExpense(
                eq(employee.getId()), eq(foodCategory.getId()), any(LocalDate.class), any(LocalDate.class), eq(expense.getId()));
        verify(eventPublisher, times(2)).publishBudgetExceededEvent(any());
    }
}