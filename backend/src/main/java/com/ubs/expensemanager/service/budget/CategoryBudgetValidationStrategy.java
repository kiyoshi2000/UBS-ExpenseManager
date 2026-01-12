package com.ubs.expensemanager.service.budget;

import com.ubs.expensemanager.event.BudgetExceededEvent;
import com.ubs.expensemanager.event.EventPublisher;
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
 * Strategy implementation for validating category budget limits.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryBudgetValidationStrategy implements BudgetValidationStrategy {

    private final ExpenseRepository expenseRepository;
    private final EventPublisher eventPublisher;

    @Override
    public void validate(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        validateDailyBudget(userId, category, expense, newAmount);
        validateMonthlyBudget(userId, category, expense, newAmount);
    }

    /**
     * Validates daily budget limits for the category.
     */
    private void validateDailyBudget(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        BigDecimal dailyTotal = Optional.ofNullable(
                expenseRepository.sumAmountByUserAndCategoryAndDateExcludingExpense(userId, category.getId(), expense.getExpenseDate(), expense.getId())
        ).orElse(BigDecimal.ZERO);
        BigDecimal newDailyTotal = dailyTotal.add(newAmount);

        if (newDailyTotal.compareTo(category.getDailyBudget()) > 0) {
            log.warn("Daily budget exceeded for user {} in category {} on {}: current={}, new={}, limit={}",
                    userId, category.getName(), expense.getExpenseDate(), dailyTotal, newDailyTotal, category.getDailyBudget());

            // Publish domain event for daily budget exceeded
            BudgetExceededEvent event = BudgetExceededEvent.builder()
                    .budgetType(BudgetExceededEvent.BudgetType.CATEGORY)
                    .expense(expense)
                    .category(category)
                    .userId(userId)
                    .currentTotal(dailyTotal)
                    .newTotal(newDailyTotal)
                    .budgetLimit(category.getDailyBudget())
                    .date(expense.getExpenseDate())
                    .build();

            eventPublisher.publishBudgetExceededEvent(event);
        }
    }

    /**
     * Validates monthly budget limits for the category.
     */
    private void validateMonthlyBudget(Long userId, ExpenseCategory category, Expense expense, BigDecimal newAmount) {
        YearMonth yearMonth = YearMonth.from(expense.getExpenseDate());
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        BigDecimal monthlyTotal = Optional.ofNullable(
                expenseRepository.sumAmountByUserAndCategoryAndDateRangeExcludingExpense(userId, category.getId(), monthStart, monthEnd, expense.getId())
        ).orElse(BigDecimal.ZERO);

        BigDecimal newMonthlyTotal = monthlyTotal.add(newAmount);

        if (newMonthlyTotal.compareTo(category.getMonthlyBudget()) > 0) {
            log.warn("Monthly budget exceeded for user {} in category {} in {}: current={}, new={}, limit={}",
                    userId, category.getName(), yearMonth, monthlyTotal, newMonthlyTotal, category.getMonthlyBudget());

            // Publish domain event for monthly budget exceeded
            BudgetExceededEvent event = BudgetExceededEvent.builder()
                    .budgetType(BudgetExceededEvent.BudgetType.CATEGORY)
                    .expense(expense)
                    .category(category)
                    .userId(userId)
                    .currentTotal(monthlyTotal)
                    .newTotal(newMonthlyTotal)
                    .budgetLimit(category.getMonthlyBudget())
                    .yearMonth(yearMonth)
                    .build();

            eventPublisher.publishBudgetExceededEvent(event);
        }
    }
}
