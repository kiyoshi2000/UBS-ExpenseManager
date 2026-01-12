package com.ubs.expensemanager.event;

import com.ubs.expensemanager.model.Alert;
import com.ubs.expensemanager.model.AlertStatus;
import com.ubs.expensemanager.model.AlertType;
import com.ubs.expensemanager.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Listener for budget exceeded events that creates alerts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetExceededEventListener {

    private final AlertRepository alertRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    /**
     * Handles budget exceeded events by creating alerts.
     *
     * @param event the budget exceeded event
     */
    @EventListener
    @Transactional
    public void handleBudgetExceededEvent(BudgetExceededEvent event) {
        log.info("Handling budget exceeded event: {}", event);

        String message = createAlertMessage(event);
        AlertType alertType = event.getBudgetType() == BudgetExceededEvent.BudgetType.DEPARTAMENT 
                ? AlertType.DEPARTMENT 
                : AlertType.CATEGORY;

        // If this is a DEPARTMENT alert, check if there's already a CATEGORY alert for this expense
        if (alertType == AlertType.DEPARTMENT) {
            Optional<Alert> existingAlert = alertRepository.findByExpenseAndTypeAndStatus(
                    event.getExpense(), AlertType.CATEGORY, AlertStatus.NEW);

            if (existingAlert.isPresent()) {
                // Update existing alert instead of creating a new one
                Alert alert = existingAlert.get();
                alert.setType(AlertType.ALL);
                alertRepository.save(alert);
                log.info("Updated existing alert to ALL type: {}", alert);
                return;
            }
        }

        // Create a new alert if no existing alert was found or if this is a CATEGORY alert
        Alert alert = Alert.builder()
                .type(alertType)
                .message(message)
                .expense(event.getExpense())
                .build();

        alertRepository.save(alert);
        log.info("Created new alert: {}", alert);
    }

    /**
     * Creates an appropriate alert message based on the budget exceeded event.
     *
     * @param event the budget exceeded event
     * @return the alert message
     */
    private String createAlertMessage(BudgetExceededEvent event) {
        String timeFrame;
        String formattedTime;

        // Determine if this is a daily or monthly budget based on which field is populated
        if (event.getYearMonth() != null) {
            // Monthly budget exceeded
            timeFrame = "Monthly";
            formattedTime = event.getYearMonth().format(MONTH_FORMATTER);
        } else if (event.getDate() != null) {
            // Daily budget exceeded
            timeFrame = "Daily";
            formattedTime = event.getDate().format(DATE_FORMATTER);
        } else {
            // Fallback if both are null (shouldn't happen)
            timeFrame = "Unknown";
            formattedTime = "unknown date";
        }

        return String.format(
                "%s budget exceeded for category '%s' on %s. " +
                "Current total: %s, New total: %s, Budget limit: %s",
                timeFrame,
                event.getCategory().getName(),
                formattedTime,
                event.getCurrentTotal(),
                event.getNewTotal(),
                event.getBudgetLimit()
        );
    }
}
