package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.ExpenseCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseFilterRequest;
import com.ubs.expensemanager.dto.request.ExpenseUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseAuditResponse;
import com.ubs.expensemanager.dto.response.ExpenseResponse;
import com.ubs.expensemanager.exception.InvalidStatusTransitionException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.exception.UnauthorizedExpenseAccessException;
import com.ubs.expensemanager.mapper.ExpenseMapper;
import com.ubs.expensemanager.model.*;
import com.ubs.expensemanager.repository.CurrencyRepository;
import com.ubs.expensemanager.repository.ExpenseCategoryRepository;
import com.ubs.expensemanager.repository.ExpenseRepository;
import com.ubs.expensemanager.repository.specification.ExpenseSpecifications;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Service responsible for handling business logic related to Expenses.
 *
 * <p>This class orchestrates validation, persistence, transformation,
 * budget validation, and status transitions for expenses.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final CurrencyRepository currencyRepository;
    private final ExpenseMapper expenseMapper;
    private final EntityManager entityManager;

    /**
     * Creates a new expense with budget validation.
     *
     * @param request data required to create an expense
     * @return created expense as response DTO
     */
    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        User currentUser = getCurrentUser();
        log.info("Creating expense for user {} in category {}", currentUser.getId(), request.getExpenseCategoryId());

        ExpenseCategory category = expenseCategoryRepository.findById(request.getExpenseCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found"));

        Currency currency = currencyRepository.findByName(request.getCurrencyName())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + request.getCurrencyName()));

        // Perform budget validation (warnings only, does not block creation)
        validateBudget(currentUser.getId(), category, request.getExpenseDate(), request.getAmount());

        Expense expense = expenseMapper.toEntity(request, currency, category, currentUser, ExpenseStatus.PENDING);

        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense {} created successfully with status PENDING", savedExpense.getId());

        return expenseMapper.toResponse(savedExpense);
    }

    /**
     * Retrieves all expenses with filtering and pagination.
     * EMPLOYEE role: Only sees own expenses
     * MANAGER/FINANCE: Can see all expenses
     *
     * @param filters optional filters for expenses
     * @param pageable pagination parameters
     * @return page of expenses
     */
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> findAll(ExpenseFilterRequest filters, Pageable pageable) {
        User currentUser = getCurrentUser();
        Specification<Expense> spec = Specification.where(null);

        // EMPLOYEE can only see their own expenses
        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            spec = spec.and(ExpenseSpecifications.withUserId(currentUser.getId()));
            log.debug("Filtering expenses for EMPLOYEE user {}", currentUser.getId());
        } else if (filters.getUserId() != null) {
            // MANAGER/FINANCE can filter by userId if provided
            spec = spec.and(ExpenseSpecifications.withUserId(filters.getUserId()));
            log.debug("Filtering expenses for user {} (requested by MANAGER/FINANCE)", filters.getUserId());
        }

        spec = spec.and(ExpenseSpecifications.withStatus(filters.getStatus()));
        spec = spec.and(ExpenseSpecifications.withStartDate(filters.getStartDate()));
        spec = spec.and(ExpenseSpecifications.withEndDate(filters.getEndDate()));
        spec = spec.and(ExpenseSpecifications.withExpenseCategoryId(filters.getExpenseCategoryId()));

        return expenseRepository.findAll(spec, pageable).map(expenseMapper::toResponse);
    }

    /**
     * Retrieves a single expense by ID with authorization check.
     * EMPLOYEE: Can only view own expenses
     * MANAGER/FINANCE: Can view any expense
     *
     * @param id expense identifier
     * @return expense as response DTO
     */
    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        validateAccess(expense);
        return expenseMapper.toResponse(expense);
    }

    /**
     * Updates an existing expense.
     * Only allowed for PENDING or REQUIRES_REVISION status.
     * Only the expense owner can update.
     *
     * @param id expense identifier
     * @param request updated expense data
     * @return updated expense as response DTO
     */
    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        // Validate ownership
        validateOwnership(expense);

        // Validate status - only PENDING or REQUIRES_REVISION can be updated
        if (expense.getStatus() != ExpenseStatus.PENDING && expense.getStatus() != ExpenseStatus.REQUIRES_REVISION) {
            throw new InvalidStatusTransitionException(
                    "Cannot update expense with status " + expense.getStatus() + ". Only PENDING or REQUIRES_REVISION expenses can be updated."
            );
        }

        log.info("Updating expense {} by user {}", id, currentUser.getId());

        ExpenseCategory category = expenseCategoryRepository.findById(request.getExpenseCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found"));

        Currency currency = currencyRepository.findByName(request.getCurrencyName())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + request.getCurrencyName()));

        expense = expenseMapper.updateEntity(expense, request, currency, category, ExpenseStatus.PENDING);

        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    /**
     * Deletes an expense.
     * Only allowed for PENDING status.
     * Only the expense owner can delete.
     *
     * @param id expense identifier
     */
    @Transactional
    public void delete(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        validateOwnership(expense);

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Cannot delete expense with status " + expense.getStatus() + ". Only PENDING expenses can be deleted."
            );
        }

        log.info("Deleting expense {} by user {}", id, currentUser.getId());
        expenseRepository.delete(expense);
    }

    /**
     * Approves an expense.
     * MANAGER: PENDING → APPROVED_BY_MANAGER
     * FINANCE: APPROVED_BY_MANAGER → APPROVED_BY_FINANCE
     *
     * @param id expense identifier
     * @return updated expense as response DTO
     */
    @Transactional
    public ExpenseResponse approve(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        ExpenseStatus currentStatus = expense.getStatus();
        log.info("User {} (role: {}) attempting to approve expense {} with status {}",
                currentUser.getId(), currentUser.getRole(), id, currentStatus);

        if (currentUser.getRole() == UserRole.MANAGER) {
            if (currentStatus != ExpenseStatus.PENDING) {
                throw new InvalidStatusTransitionException(
                        "Manager can only approve expenses with status PENDING. Current status: " + currentStatus
                );
            }

            // Validate that manager is in the same department as the employee
            if (!expense.getUser().getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new UnauthorizedExpenseAccessException(
                        "You can only approve expenses for employees in your department"
                );
            }

            expense.setStatus(ExpenseStatus.APPROVED_BY_MANAGER);
            log.info("Manager approved expense {}. Status: PENDING → APPROVED_BY_MANAGER", id);

        } else if (currentUser.getRole() == UserRole.FINANCE) {
            if (currentStatus != ExpenseStatus.APPROVED_BY_MANAGER) {
                throw new InvalidStatusTransitionException(
                        "Finance can only approve expenses with status APPROVED_BY_MANAGER. Current status: " + currentStatus
                );
            }
            expense.setStatus(ExpenseStatus.APPROVED_BY_FINANCE);
            log.info("Finance approved expense {}. Status: APPROVED_BY_MANAGER → APPROVED_BY_FINANCE", id);

        } else {
            throw new InvalidStatusTransitionException("Only MANAGER or FINANCE can approve expenses");
        }

        // TODO: implement permission_audit table logging for approvals

        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    /**
     * Rejects an expense.
     * MANAGER: PENDING → REJECTED
     * FINANCE: APPROVED_BY_MANAGER → REJECTED
     *
     * @param id expense identifier
     * @return updated expense as response DTO
     */
    @Transactional
    public ExpenseResponse reject(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        ExpenseStatus currentStatus = expense.getStatus();
        log.info("User {} (role: {}) attempting to reject expense {} with status {}",
                currentUser.getId(), currentUser.getRole(), id, currentStatus);

        if (currentUser.getRole() == UserRole.MANAGER) {
            if (currentStatus != ExpenseStatus.PENDING && currentStatus != ExpenseStatus.REQUIRES_REVISION) {
                throw new InvalidStatusTransitionException(
                        "Manager can only reject expenses with status PENDING or REQUIRES_REVISION. Current status: " + currentStatus
                );
            }

            // Validate that manager is in the same department as the employee
            if (!expense.getUser().getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new UnauthorizedExpenseAccessException(
                        "You can only reject expenses for employees in your department"
                );
            }
        } else if (currentUser.getRole() == UserRole.FINANCE) {
            if (currentStatus != ExpenseStatus.APPROVED_BY_MANAGER && currentStatus != ExpenseStatus.REQUIRES_REVISION) {
                throw new InvalidStatusTransitionException(
                        "Finance can only reject expenses with status APPROVED_BY_MANAGER or REQUIRES_REVISION. Current status: " + currentStatus
                );
            }
        } else {
            throw new InvalidStatusTransitionException("Only MANAGER or FINANCE can reject expenses");
        }

        expense.setStatus(ExpenseStatus.REJECTED);
        log.info("Expense {} rejected by {} with role {}", id, currentUser.getId(), currentUser.getRole());

        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    /**
     * Requests revision for an expense.
     * MANAGER/FINANCE: Any status except REJECTED or APPROVED_BY_FINANCE → REQUIRES_REVISION
     *
     * @param id expense identifier
     * @return updated expense as response DTO
     */
    @Transactional
    public ExpenseResponse requestRevision(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        ExpenseStatus currentStatus = expense.getStatus();
        log.info("User {} (role: {}) requesting revision for expense {} with status {}",
                currentUser.getId(), currentUser.getRole(), id, currentStatus);

        if (currentStatus == ExpenseStatus.REJECTED || currentStatus == ExpenseStatus.APPROVED_BY_FINANCE) {
            throw new InvalidStatusTransitionException(
                    "Cannot request revision for expense with status " + currentStatus
            );
        }

        // Validate that manager is in the same department as the employee
        if (currentUser.getRole() == UserRole.MANAGER &&
                !expense.getUser().getDepartment().getId().equals(currentUser.getDepartment().getId())) {
            throw new UnauthorizedExpenseAccessException(
                    "You can only request revision for expenses from employees in your department"
            );
        }

        expense.setStatus(ExpenseStatus.REQUIRES_REVISION);
        log.info("Expense {} status changed to REQUIRES_REVISION", id);

        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    /**
     * Validates budget limits (daily and monthly) for the expense.
     * Logs warnings if budget is exceeded but does not block creation.
     * TODO: Future enhancement - trigger alert/notification system
     *
     * @param userId user ID
     * @param category expense category
     * @param expenseDate date of the expense
     * @param newAmount amount of the new expense
     */
    private void validateBudget(Long userId, ExpenseCategory category, LocalDate expenseDate, BigDecimal newAmount) {
        // Daily budget check
        BigDecimal dailyTotal = expenseRepository.sumAmountByUserAndCategoryAndDate(
                userId, category.getId(), expenseDate
        );
        BigDecimal newDailyTotal = dailyTotal.add(newAmount);

        if (newDailyTotal.compareTo(category.getDailyBudget()) > 0) {
            log.warn("Daily budget exceeded for user {} in category {} on {}: current={}, new={}, limit={}",
                    userId, category.getName(), expenseDate, dailyTotal, newDailyTotal, category.getDailyBudget());
            // TODO: Trigger alert system
        }

        // Monthly budget check
        YearMonth yearMonth = YearMonth.from(expenseDate);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        BigDecimal monthlyTotal = expenseRepository.sumAmountByUserAndCategoryAndDateRange(
                userId, category.getId(), monthStart, monthEnd
        );
        BigDecimal newMonthlyTotal = monthlyTotal.add(newAmount);

        if (newMonthlyTotal.compareTo(category.getMonthlyBudget()) > 0) {
            log.warn("Monthly budget exceeded for user {} in category {} in {}: current={}, new={}, limit={}",
                    userId, category.getName(), yearMonth, monthlyTotal, newMonthlyTotal, category.getMonthlyBudget());
            // TODO: Trigger alert system
        }
    }

    /**
     * Gets the currently authenticated user from SecurityContext.
     *
     * @return the current User
     */
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Validates that the current user can access the expense.
     * EMPLOYEE: Can only access own expenses
     * MANAGER/FINANCE: Can access any expense
     *
     * @param expense the expense to check
     * @throws UnauthorizedExpenseAccessException if access is denied
     */
    private void validateAccess(Expense expense) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == UserRole.EMPLOYEE &&
                !expense.getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to access expense {} owned by user {}",
                    currentUser.getId(), expense.getId(), expense.getUser().getId());
            throw new UnauthorizedExpenseAccessException("You do not have permission to access this expense");
        }
    }

    /**
     * Validates that the current user owns the expense.
     * Used for update and delete operations.
     *
     * @param expense the expense to check
     * @throws UnauthorizedExpenseAccessException if ownership check fails
     */
    private void validateOwnership(Expense expense) {
        User currentUser = getCurrentUser();

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to modify expense {} owned by user {}",
                    currentUser.getId(), expense.getId(), expense.getUser().getId());
            throw new UnauthorizedExpenseAccessException("You do not have permission to modify this expense");
        }
    }

    /**
     * Retrieves the complete audit history for an expense (asc order).
     *
     * @param id expense identifier
     * @return list of all audited versions of the expense
     */
    @SuppressWarnings("unchecked")
    public List<ExpenseAuditResponse> getAuditHistory(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found");
        }

        var auditReader = AuditReaderFactory.get(entityManager);
        
        List<Object[]> results = auditReader.createQuery()
                .forRevisionsOfEntity(Expense.class, false, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();
        
        return results.stream()
                .map(result -> {
                    Expense entity = (Expense) result[0];
                    Number revNumber = (Number) ((org.hibernate.envers.DefaultRevisionEntity) result[1]).getId();
                    long revTimestamp = ((org.hibernate.envers.DefaultRevisionEntity) result[1]).getTimestamp();
                    RevisionType revType = (RevisionType) result[2];

                    return ExpenseAuditResponse.builder()
                            .id(entity.getId())
                            .amount(entity.getAmount())
                            .description(entity.getDescription())
                            .expenseDate(entity.getExpenseDate())
                            .userId(entity.getUser().getId())
                            .userName(entity.getUser().getName())
                            .expenseCategoryId(entity.getExpenseCategory().getId())
                            .expenseCategoryName(entity.getExpenseCategory().getName())
                            .currencyName(entity.getCurrency().getName())
                            .exchangeRate(entity.getCurrency().getExchangeRate())
                            .receiptUrl(entity.getReceiptUrl())
                            .status(entity.getStatus())
                            .revisionNumber(revNumber)
                            .revisionType((short) revType.ordinal())
                            .revisionDate(java.time.LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(revTimestamp), ZoneId.systemDefault()))
                            .build();
                })
                .collect(Collectors.toList());
    }
}
