package com.ubs.expensemanager.event;

import com.ubs.expensemanager.model.Alert;
import com.ubs.expensemanager.model.AlertStatus;
import com.ubs.expensemanager.model.AlertType;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.AlertRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetExceededEventListenerTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private BudgetExceededEventListener eventListener;

    @Captor
    private ArgumentCaptor<Alert> alertCaptor;

    private User employee;
    private Department itDepartment;
    private ExpenseCategory foodCategory;
    private Currency usdCurrency;
    private Expense expense;
    private BudgetExceededEvent categoryEvent;
    private BudgetExceededEvent departmentEvent;

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

        // Setup Category Budget Exceeded Event
        categoryEvent = BudgetExceededEvent.builder()
                .budgetType(BudgetExceededEvent.BudgetType.CATEGORY)
                .expense(expense)
                .category(foodCategory)
                .userId(employee.getId())
                .currentTotal(BigDecimal.valueOf(60))
                .newTotal(BigDecimal.valueOf(110))
                .budgetLimit(BigDecimal.valueOf(100))
                .date(LocalDate.now())
                .build();

        // Setup Department Budget Exceeded Event
        departmentEvent = BudgetExceededEvent.builder()
                .budgetType(BudgetExceededEvent.BudgetType.DEPARTAMENT)
                .expense(expense)
                .category(foodCategory)
                .userId(employee.getId())
                .currentTotal(BigDecimal.valueOf(360))
                .newTotal(BigDecimal.valueOf(410))
                .budgetLimit(BigDecimal.valueOf(400))
                .date(LocalDate.now())
                .build();
    }

    @Test
    void handleBudgetExceededEvent_categoryEvent_createsNewAlert() {
        // Given
        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.CATEGORY), eq(AlertStatus.NEW)))
                .thenReturn(List.of());
        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.ALL), eq(AlertStatus.NEW)))
                .thenReturn(List.of());

        // When
        eventListener.handleBudgetExceededEvent(categoryEvent);

        // Then
        verify(alertRepository).save(alertCaptor.capture());
        Alert savedAlert = alertCaptor.getValue();
        assertEquals(AlertType.CATEGORY, savedAlert.getType());
        assertEquals(expense, savedAlert.getExpense());
        assertEquals(AlertStatus.NEW, savedAlert.getStatus());
        assertNotNull(savedAlert.getMessage());
    }

    @Test
    void handleBudgetExceededEvent_departmentEvent_createsNewAlert() {
        // Given
        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.CATEGORY), eq(AlertStatus.NEW)))
                .thenReturn(List.of());
        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.ALL), eq(AlertStatus.NEW)))
                .thenReturn(List.of());

        // When
        eventListener.handleBudgetExceededEvent(departmentEvent);

        // Then
        verify(alertRepository).save(alertCaptor.capture());
        Alert savedAlert = alertCaptor.getValue();
        assertEquals(AlertType.DEPARTMENT, savedAlert.getType());
        assertEquals(expense, savedAlert.getExpense());
        assertEquals(AlertStatus.NEW, savedAlert.getStatus());
        assertNotNull(savedAlert.getMessage());
    }

    @Test
    void handleBudgetExceededEvent_departmentEventWithExistingCategoryAlert_updatesExistingAlert() {
        // Given
        Alert existingAlert = Alert.builder()
                .id(1L)
                .type(AlertType.CATEGORY)
                .message("Daily budget exceeded for category 'Food' on 2026-01-08")
                .status(AlertStatus.NEW)
                .expense(expense)
                .build();

        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.CATEGORY), eq(AlertStatus.NEW)))
                .thenReturn(List.of(existingAlert));
        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.ALL), eq(AlertStatus.NEW)))
                .thenReturn(List.of());

        // When
        eventListener.handleBudgetExceededEvent(departmentEvent);

        // Then
        verify(alertRepository).save(alertCaptor.capture());
        Alert savedAlert = alertCaptor.getValue();
        assertEquals(existingAlert.getId(), savedAlert.getId());
        assertEquals(AlertType.ALL, savedAlert.getType());
        assertEquals(expense, savedAlert.getExpense());
        assertEquals(AlertStatus.NEW, savedAlert.getStatus());
        // Verify that the message was updated to include department information
        assertTrue(savedAlert.getMessage().contains("Daily budget exceeded for category 'Food'"));
        assertTrue(savedAlert.getMessage().contains("Daily budget exceeded for department"));
    }

    @Test
    void handleBudgetExceededEvent_multipleCategoryEvents_consolidatesIntoSingleAlert() {
        // Given - First event creates an alert
        BudgetExceededEvent dailyBudgetEvent = BudgetExceededEvent.builder()
                .budgetType(BudgetExceededEvent.BudgetType.CATEGORY)
                .expense(expense)
                .category(foodCategory)
                .userId(employee.getId())
                .currentTotal(BigDecimal.valueOf(60))
                .newTotal(BigDecimal.valueOf(110))
                .budgetLimit(BigDecimal.valueOf(100))
                .date(LocalDate.now())
                .build();

        BudgetExceededEvent monthlyBudgetEvent = BudgetExceededEvent.builder()
                .budgetType(BudgetExceededEvent.BudgetType.CATEGORY)
                .expense(expense)
                .category(foodCategory)
                .userId(employee.getId())
                .currentTotal(BigDecimal.valueOf(2980))
                .newTotal(BigDecimal.valueOf(3030))
                .budgetLimit(BigDecimal.valueOf(3000))
                .yearMonth(YearMonth.now())
                .build();

        // First event - no existing alerts
        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.CATEGORY), eq(AlertStatus.NEW)))
                .thenReturn(List.of());
        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.ALL), eq(AlertStatus.NEW)))
                .thenReturn(List.of());

        // When - handle first event
        eventListener.handleBudgetExceededEvent(dailyBudgetEvent);

        // Then - verify first alert created
        verify(alertRepository, times(1)).save(any(Alert.class));

        // Given - Second event finds the first alert
        Alert firstAlert = Alert.builder()
                .id(1L)
                .type(AlertType.CATEGORY)
                .message("Daily budget exceeded for category 'Food' on 2026-01-08. Current total: 60, New total: 110, Budget limit: 100")
                .status(AlertStatus.NEW)
                .expense(expense)
                .build();

        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.CATEGORY), eq(AlertStatus.NEW)))
                .thenReturn(List.of(firstAlert));
        when(alertRepository.findAllByExpenseAndTypeAndStatus(
                eq(expense), eq(AlertType.ALL), eq(AlertStatus.NEW)))
                .thenReturn(List.of());

        // When - handle second event
        eventListener.handleBudgetExceededEvent(monthlyBudgetEvent);

        // Then - verify alert was updated (2 saves total: 1 create + 1 update)
        verify(alertRepository, times(2)).save(alertCaptor.capture());
        List<Alert> savedAlerts = alertCaptor.getAllValues();
        Alert updatedAlert = savedAlerts.get(1);
        
        assertEquals(firstAlert.getId(), updatedAlert.getId());
        assertEquals(AlertType.CATEGORY, updatedAlert.getType()); // Still CATEGORY, not DEPARTMENT
        assertTrue(updatedAlert.getMessage().contains("Daily budget exceeded"));
        assertTrue(updatedAlert.getMessage().contains("Monthly budget exceeded"));
    }

    // We can't directly test the private createAlertMessage method,
    // but we can verify that the alert message contains the expected information
    // through the handleBudgetExceededEvent tests above.
}
