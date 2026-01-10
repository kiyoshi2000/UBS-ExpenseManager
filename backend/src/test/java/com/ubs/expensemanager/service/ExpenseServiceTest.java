package com.ubs.expensemanager.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ubs.expensemanager.dto.request.ExpenseCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseFilterRequest;
import com.ubs.expensemanager.dto.request.ExpenseUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseResponse;
import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.exception.UnauthorizedExpenseAccessException;
import com.ubs.expensemanager.mapper.ExpenseMapper;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.CurrencyRepository;
import com.ubs.expensemanager.repository.ExpenseCategoryRepository;
import com.ubs.expensemanager.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

  @Mock
  ExpenseRepository expenseRepository;

  @Mock
  ExpenseCategoryRepository expenseCategoryRepository;

  @Mock
  CurrencyRepository currencyRepository;

  @Mock
  ExpenseMapper expenseMapper;

  @Mock
  SecurityContext securityContext;

  @Mock
  Authentication authentication;

  @InjectMocks
  ExpenseService expenseService;

  User employee;
  User manager;
  User managerOtherDept;
  User finance;
  Department itDepartment;
  Department financeDepartment;
  ExpenseCategory foodCategory;
  Currency usdCurrency;
  Expense pendingExpense;
  Expense approvedByManagerExpense;
  ExpenseResponse expenseResponse;

  @BeforeEach
  void setUp() {
    // Setup Departments
    itDepartment = Department.builder()
        .id(1L)
        .name("IT")
        .dailyBudget(BigDecimal.valueOf(400))
        .monthlyBudget(BigDecimal.valueOf(12000))
        .build();

    financeDepartment = Department.builder()
        .id(2L)
        .name("Finance")
        .dailyBudget(BigDecimal.valueOf(500))
        .monthlyBudget(BigDecimal.valueOf(15000))
        .build();

    // Setup Users
    employee = User.builder()
        .id(1L)
        .name("John Employee")
        .email("employee@ubs.com")
        .role(UserRole.EMPLOYEE)
        .department(itDepartment)
        .active(true)
        .build();

    manager = User.builder()
        .id(2L)
        .name("Jane Manager")
        .email("manager@ubs.com")
        .role(UserRole.MANAGER)
        .department(itDepartment)
        .active(true)
        .build();

    managerOtherDept = User.builder()
        .id(3L)
        .name("Bob Manager")
        .email("manager2@ubs.com")
        .role(UserRole.MANAGER)
        .department(financeDepartment)
        .active(true)
        .build();

    finance = User.builder()
        .id(4L)
        .name("Alice Finance")
        .email("finance@ubs.com")
        .role(UserRole.FINANCE)
        .department(financeDepartment)
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

    // Setup Expenses
    pendingExpense = Expense.builder()
        .id(1L)
        .amount(BigDecimal.valueOf(50))
        .description("Team lunch")
        .expenseDate(LocalDate.now())
        .user(employee)
        .expenseCategory(foodCategory)
        .currency(usdCurrency)
        .status(ExpenseStatus.PENDING)
        .build();

    approvedByManagerExpense = Expense.builder()
        .id(2L)
        .amount(BigDecimal.valueOf(75))
        .description("Client dinner")
        .expenseDate(LocalDate.now())
        .user(employee)
        .expenseCategory(foodCategory)
        .currency(usdCurrency)
        .status(ExpenseStatus.APPROVED_BY_MANAGER)
        .build();

    expenseResponse = ExpenseResponse.builder()
        .id(1L)
        .amount(BigDecimal.valueOf(50))
        .description("Team lunch")
        .userId(1L)
        .userName("John Employee")
        .status(ExpenseStatus.PENDING)
        .build();

    SecurityContextHolder.setContext(securityContext);
  }

  // ==================== CREATE TESTS ====================

  @Test
  void create_Success() {
    ExpenseCreateRequest request = ExpenseCreateRequest.builder()
        .amount(BigDecimal.valueOf(50))
        .description("Team lunch")
        .expenseDate(LocalDate.now())
        .expenseCategoryId(1L)
        .currencyName("USD")
        .build();

    Expense expense = Expense.builder()
        .amount(BigDecimal.valueOf(50))
        .description("Team lunch")
        .expenseDate(LocalDate.now())
        .user(employee)
        .expenseCategory(foodCategory)
        .currency(usdCurrency)
        .status(ExpenseStatus.PENDING)
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseCategoryRepository.findById(1L)).thenReturn(Optional.of(foodCategory));
    when(currencyRepository.findByName("USD")).thenReturn(Optional.of(usdCurrency));
    when(expenseRepository.sumAmountByUserAndCategoryAndDate(any(), any(), any()))
        .thenReturn(BigDecimal.ZERO);
    when(expenseRepository.sumAmountByUserAndCategoryAndDateRange(any(), any(), any(), any()))
        .thenReturn(BigDecimal.ZERO);
    when(expenseRepository.save(any(Expense.class))).thenReturn(pendingExpense);
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);
    when(expenseMapper.toEntity(any(ExpenseCreateRequest.class), any(Currency.class),
        any(ExpenseCategory.class), any(User.class), any(ExpenseStatus.class)))
        .thenReturn(expense);

    ExpenseResponse result = expenseService.create(request);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(expenseResponse.getId(), result.getId()),
        () -> verify(expenseRepository).save(any(Expense.class))
    );
  }

  @Test
  void create_CategoryNotFound_ThrowsException() {
    ExpenseCreateRequest request = ExpenseCreateRequest.builder()
        .expenseCategoryId(999L)
        .currencyName("USD")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseCategoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertAll(
        () -> assertThrows(ResourceNotFoundException.class, () -> expenseService.create(request)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  @Test
  void create_CurrencyNotFound_ThrowsException() {
    ExpenseCreateRequest request = ExpenseCreateRequest.builder()
        .expenseCategoryId(1L)
        .currencyName("XXX")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseCategoryRepository.findById(1L)).thenReturn(Optional.of(foodCategory));
    when(currencyRepository.findByName("XXX")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> expenseService.create(request));
    verify(expenseRepository, never()).save(any());
  }

  // ==================== FINDALL TESTS ====================

  @Test
  void findAll_AsEmployee_OnlySeesOwnExpenses() {
    ExpenseFilterRequest filters = new ExpenseFilterRequest();
    Pageable pageable = PageRequest.of(0, 10);
    Page<Expense> expensePage = new PageImpl<>(List.of(pendingExpense));

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(expensePage);
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);

    Page<ExpenseResponse> result = expenseService.findAll(filters, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(expenseRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void findAll_AsManager_CanSeeAllExpenses() {
    ExpenseFilterRequest filters = new ExpenseFilterRequest();
    Pageable pageable = PageRequest.of(0, 10);
    Page<Expense> expensePage = new PageImpl<>(List.of(pendingExpense));

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(manager);
    when(expenseRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(expensePage);
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);

    Page<ExpenseResponse> result = expenseService.findAll(filters, pageable);

    assertNotNull(result);
    verify(expenseRepository).findAll(any(Specification.class), eq(pageable));
  }

  // ==================== FINDBYID TESTS ====================

  @Test
  void findById_AsOwner_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);

    ExpenseResponse result = expenseService.findById(1L);

    assertNotNull(result);
    verify(expenseRepository).findById(1L);
  }

  @Test
  void findById_AsEmployeeNotOwner_ThrowsException() {
    User otherEmployee = User.builder()
        .id(99L)
        .role(UserRole.EMPLOYEE)
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(otherEmployee);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    assertThrows(UnauthorizedExpenseAccessException.class, () -> expenseService.findById(1L));
  }

  @Test
  void findById_AsManager_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(manager);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);

    ExpenseResponse result = expenseService.findById(1L);

    assertNotNull(result);
  }

  @Test
  void findById_NotFound_ThrowsException() {
    when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> expenseService.findById(999L));
  }

  // ==================== UPDATE TESTS ====================

  @Test
  void update_AsPendingOwner_Success() {
    ExpenseUpdateRequest request = ExpenseUpdateRequest.builder()
        .amount(BigDecimal.valueOf(60))
        .description("Updated description")
        .expenseDate(LocalDate.now())
        .expenseCategoryId(1L)
        .currencyName("USD")
        .build();

    Expense expense = Expense.builder()
        .id(1L)
        .user(employee)
        .status(ExpenseStatus.PENDING)
        .expenseCategory(foodCategory)
        .currency(usdCurrency)
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));
    when(expenseCategoryRepository.findById(1L)).thenReturn(Optional.of(foodCategory));
    when(currencyRepository.findByName("USD")).thenReturn(Optional.of(usdCurrency));
    when(expenseRepository.save(any(Expense.class))).thenReturn(pendingExpense);
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);
    when(expenseMapper.updateEntity(any(Expense.class), any(ExpenseUpdateRequest.class),
        any(Currency.class), any(ExpenseCategory.class), any(ExpenseStatus.class))).thenReturn(
        expense);

    ExpenseResponse result = expenseService.update(1L, request);

    assertAll(
        () -> assertNotNull(result),
        () -> verify(expenseRepository).save(any(Expense.class))
    );
  }

  @Test
  void update_AsRequiresRevisionOwner_ResetsStatusToPending() {
    Expense requiresRevisionExpense = Expense.builder()
        .id(1L)
        .user(employee)
        .status(ExpenseStatus.REQUIRES_REVISION)
        .expenseCategory(foodCategory)
        .currency(usdCurrency)
        .build();

    ExpenseUpdateRequest request = ExpenseUpdateRequest.builder()
        .amount(BigDecimal.valueOf(60))
        .expenseDate(LocalDate.now())
        .expenseCategoryId(1L)
        .currencyName("USD")
        .build();

    Expense expense = Expense.builder()
        .id(1L)
        .user(employee)
        .status(ExpenseStatus.PENDING)
        .expenseCategory(foodCategory)
        .currency(usdCurrency)
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(requiresRevisionExpense));
    when(expenseCategoryRepository.findById(1L)).thenReturn(Optional.of(foodCategory));
    when(currencyRepository.findByName("USD")).thenReturn(Optional.of(usdCurrency));
    when(expenseRepository.save(any(Expense.class))).thenReturn(requiresRevisionExpense);
    when(expenseMapper.toResponse(requiresRevisionExpense)).thenReturn(expenseResponse);
    when(expenseMapper.updateEntity(any(Expense.class), any(ExpenseUpdateRequest.class),
        any(Currency.class), any(ExpenseCategory.class), any(ExpenseStatus.class))).thenReturn(
        expense);

    ExpenseResponse result = expenseService.update(1L, request);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(ExpenseStatus.PENDING, result.getStatus()),
        () -> verify(expenseRepository).save(any(Expense.class))
    );
  }

  @Test
  void update_AsNonOwner_ThrowsException() {
    ExpenseUpdateRequest request = ExpenseUpdateRequest.builder()
        .expenseCategoryId(1L)
        .currencyName("USD")
        .build();

    User otherEmployee = User.builder()
        .id(99L)
        .role(UserRole.EMPLOYEE)
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(otherEmployee);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    assertAll(
        () -> assertThrows(UnauthorizedExpenseAccessException.class,
            () -> expenseService.update(1L, request)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  @Test
  void update_ApprovedExpense_ThrowsException() {
    ExpenseUpdateRequest request = ExpenseUpdateRequest.builder()
        .expenseCategoryId(1L)
        .currencyName("USD")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseRepository.findById(2L)).thenReturn(Optional.of(approvedByManagerExpense));

    assertAll(
        () -> assertThrows(InvalidStatusTransitionException.class,
            () -> expenseService.update(2L, request)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  // ==================== DELETE TESTS ====================

  @Test
  void delete_AsPendingOwner_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    expenseService.delete(1L);

    verify(expenseRepository).delete(pendingExpense);
  }

  @Test
  void delete_AsNonOwner_ThrowsException() {
    User otherEmployee = User.builder()
        .id(99L)
        .role(UserRole.EMPLOYEE)
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(otherEmployee);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    assertAll(
        () -> assertThrows(UnauthorizedExpenseAccessException.class,
            () -> expenseService.delete(1L)),
        () -> verify(expenseRepository, never()).delete(any(Expense.class))
    );
  }

  @Test
  void delete_ApprovedExpense_ThrowsException() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseRepository.findById(2L)).thenReturn(Optional.of(approvedByManagerExpense));

    assertAll(
        () -> assertThrows(InvalidStatusTransitionException.class, () -> expenseService.delete(2L)),
        () -> verify(expenseRepository, never()).delete(any(Expense.class))
    );
  }

  // ==================== APPROVE TESTS ====================

  @Test
  void approve_AsManagerSameDepartment_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(manager);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));
    when(expenseRepository.save(any(Expense.class))).thenReturn(pendingExpense);
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);

    ExpenseResponse result = expenseService.approve(1L);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(ExpenseStatus.APPROVED_BY_MANAGER, pendingExpense.getStatus()),
        () -> verify(expenseRepository).save(pendingExpense)
    );
  }

  @Test
  void approve_AsManagerDifferentDepartment_ThrowsException() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(managerOtherDept);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    assertAll(
        () -> assertThrows(UnauthorizedExpenseAccessException.class,
            () -> expenseService.approve(1L)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  @Test
  void approve_AsManagerWrongStatus_ThrowsException() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(manager);
    when(expenseRepository.findById(2L)).thenReturn(Optional.of(approvedByManagerExpense));

    assertAll(
        () -> assertThrows(InvalidStatusTransitionException.class,
            () -> expenseService.approve(2L)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  @Test
  void approve_AsFinance_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(finance);
    when(expenseRepository.findById(2L)).thenReturn(Optional.of(approvedByManagerExpense));
    when(expenseRepository.save(any(Expense.class))).thenReturn(approvedByManagerExpense);
    when(expenseMapper.toResponse(approvedByManagerExpense)).thenReturn(expenseResponse);

    ExpenseResponse result = expenseService.approve(2L);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(ExpenseStatus.APPROVED_BY_FINANCE, approvedByManagerExpense.getStatus()),
        () -> verify(expenseRepository).save(approvedByManagerExpense)
    );
  }

  @Test
  void approve_AsFinanceWrongStatus_ThrowsException() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(finance);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    assertAll(
        () -> assertThrows(InvalidStatusTransitionException.class,
            () -> expenseService.approve(1L)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  @Test
  void approve_AsEmployee_ThrowsException() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(employee);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    assertAll(
        () -> assertThrows(InvalidStatusTransitionException.class,
            () -> expenseService.approve(1L)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  // ==================== REJECT TESTS ====================

  @Test
  void reject_AsManagerSameDepartment_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(manager);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));
    when(expenseRepository.save(any(Expense.class))).thenReturn(pendingExpense);
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);

    ExpenseResponse result = expenseService.reject(1L);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(ExpenseStatus.REJECTED, pendingExpense.getStatus()),
        () -> verify(expenseRepository).save(pendingExpense)
    );
  }

  @Test
  void reject_AsManagerDifferentDepartment_ThrowsException() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(managerOtherDept);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    assertAll(
        () -> assertThrows(UnauthorizedExpenseAccessException.class,
            () -> expenseService.reject(1L)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  @Test
  void reject_AsFinance_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(finance);
    when(expenseRepository.findById(2L)).thenReturn(Optional.of(approvedByManagerExpense));
    when(expenseRepository.save(any(Expense.class))).thenReturn(approvedByManagerExpense);
    when(expenseMapper.toResponse(approvedByManagerExpense)).thenReturn(expenseResponse);

    ExpenseResponse result = expenseService.reject(2L);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(ExpenseStatus.REJECTED, approvedByManagerExpense.getStatus()),
        () -> verify(expenseRepository).save(approvedByManagerExpense)
    );
  }

  // ==================== REQUEST REVISION TESTS ====================

  @Test
  void requestRevision_AsManagerSameDepartment_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(manager);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));
    when(expenseRepository.save(any(Expense.class))).thenReturn(pendingExpense);
    when(expenseMapper.toResponse(pendingExpense)).thenReturn(expenseResponse);

    ExpenseResponse result = expenseService.requestRevision(1L);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(ExpenseStatus.REQUIRES_REVISION, pendingExpense.getStatus()),
        () -> verify(expenseRepository).save(pendingExpense)
    );
  }

  @Test
  void requestRevision_AsManagerDifferentDepartment_ThrowsException() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(managerOtherDept);
    when(expenseRepository.findById(1L)).thenReturn(Optional.of(pendingExpense));

    assertAll(
        () -> assertThrows(UnauthorizedExpenseAccessException.class,
            () -> expenseService.requestRevision(1L)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  @Test
  void requestRevision_AsFinance_Success() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(finance);
    when(expenseRepository.findById(2L)).thenReturn(Optional.of(approvedByManagerExpense));
    when(expenseRepository.save(any(Expense.class))).thenReturn(approvedByManagerExpense);
    when(expenseMapper.toResponse(approvedByManagerExpense)).thenReturn(expenseResponse);

    ExpenseResponse result = expenseService.requestRevision(2L);

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(ExpenseStatus.REQUIRES_REVISION, approvedByManagerExpense.getStatus()),
        () -> verify(expenseRepository).save(approvedByManagerExpense)
    );
  }

  @Test
  void requestRevision_RejectedExpense_ThrowsException() {
    Expense rejectedExpense = Expense.builder()
        .id(3L)
        .user(employee)
        .status(ExpenseStatus.REJECTED)
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(manager);
    when(expenseRepository.findById(3L)).thenReturn(Optional.of(rejectedExpense));

    assertAll(
        () -> assertThrows(InvalidStatusTransitionException.class,
            () -> expenseService.requestRevision(3L)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }

  @Test
  void requestRevision_ApprovedByFinanceExpense_ThrowsException() {
    Expense approvedExpense = Expense.builder()
        .id(3L)
        .user(employee)
        .status(ExpenseStatus.APPROVED_BY_FINANCE)
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(manager);
    when(expenseRepository.findById(3L)).thenReturn(Optional.of(approvedExpense));

    assertAll(
        () -> assertThrows(InvalidStatusTransitionException.class,
            () -> expenseService.requestRevision(3L)),
        () -> verify(expenseRepository, never()).save(any())
    );
  }
}
