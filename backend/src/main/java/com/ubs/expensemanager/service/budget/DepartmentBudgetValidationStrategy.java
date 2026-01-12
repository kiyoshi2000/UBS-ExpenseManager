package com.ubs.expensemanager.service.budget;

import com.ubs.expensemanager.event.BudgetExceededEvent;
import com.ubs.expensemanager.event.EventPublisher;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

/**
 * Strategy implementation for validating department budget limits.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DepartmentBudgetValidationStrategy implements BudgetValidationStrategy {

    private final ExpenseRepository expenseRepository;
    private final EventPublisher eventPublisher;

    @Override
    public void validate(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        Department department = expense.getUser().getDepartment();
        if (department != null) {
            // Only validate daily budget if the department has a daily budget limit
            if (department.getDailyBudget() != null) {
                validateDailyBudget(userId, category, expense, newAmount, department);
            }
            validateMonthlyBudget(userId, category, expense, newAmount, department);
        }
    }

    /**
     * Validates daily budget limits for the department.
     */
    private void validateDailyBudget(Long userId, ExpenseCategory category, Expense expense, 
                                    BigDecimal newAmount, Department department) {
        BigDecimal deptDailyTotal = Optional.ofNullable(
                expenseRepository.sumAmountByDepartmentAndDateExcludingExpense(department.getId(), expense.getExpenseDate(), expense.getId())
        ).orElse(BigDecimal.ZERO);

        BigDecimal newDeptDailyTotal = deptDailyTotal.add(newAmount);

        if (department.getDailyBudget() != null && newDeptDailyTotal.compareTo(department.getDailyBudget()) > 0) {
            log.warn("Daily department budget exceeded for department {} on {}: current={}, new={}, limit={}",
                    department.getName(), expense.getExpenseDate(), deptDailyTotal, newDeptDailyTotal, department.getDailyBudget());

            // Publish domain event for daily department budget exceeded
            BudgetExceededEvent event = BudgetExceededEvent.builder()
                    .budgetType(BudgetExceededEvent.BudgetType.DEPARTAMENT)
                    .expense(expense)
                    .category(category)
                    .userId(userId)
                    .currentTotal(deptDailyTotal)
                    .newTotal(newDeptDailyTotal)
                    .budgetLimit(department.getDailyBudget())
                    .date(expense.getExpenseDate())
                    .build();

            eventPublisher.publishBudgetExceededEvent(event);
        }
    }

    /**
     * Validates monthly budget limits for the department.
     */
    private void validateMonthlyBudget(Long userId, ExpenseCategory category, Expense expense, 
                                      BigDecimal newAmount, Department department) {
        YearMonth yearMonth = YearMonth.from(expense.getExpenseDate());
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        BigDecimal deptMonthlyTotal = Optional.ofNullable(
                expenseRepository.sumAmountByDepartmentAndDateRangeExcludingExpense(department.getId(), monthStart, monthEnd, expense.getId())
        ).orElse(BigDecimal.ZERO);

        BigDecimal newDeptMonthlyTotal = deptMonthlyTotal.add(newAmount);

        if (newDeptMonthlyTotal.compareTo(department.getMonthlyBudget()) > 0) {
            log.warn("Monthly department budget exceeded for department {} in {}: current={}, new={}, limit={}",
                    department.getName(), yearMonth, deptMonthlyTotal, newDeptMonthlyTotal, department.getMonthlyBudget());

            // Publish domain event for monthly department budget exceeded
            BudgetExceededEvent event = BudgetExceededEvent.builder()
                    .budgetType(BudgetExceededEvent.BudgetType.DEPARTAMENT)
                    .expense(expense)
                    .category(category)
                    .userId(userId)
                    .currentTotal(deptMonthlyTotal)
                    .newTotal(newDeptMonthlyTotal)
                    .budgetLimit(department.getMonthlyBudget())
                    .yearMonth(yearMonth)
                    .build();

            eventPublisher.publishBudgetExceededEvent(event);
        }
    }
}
