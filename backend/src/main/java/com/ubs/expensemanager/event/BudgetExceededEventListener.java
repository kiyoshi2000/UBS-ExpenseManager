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
import java.util.List;

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

        // Check if there's already an alert for this expense (regardless of type)
        // Create a mutable list to combine results from both queries
        List<Alert> existingAlerts = new java.util.ArrayList<>(
                alertRepository.findAllByExpenseAndTypeAndStatus(
                        event.getExpense(), AlertType.CATEGORY, AlertStatus.NEW));
        
        // Also check for ALL type alerts
        existingAlerts.addAll(alertRepository.findAllByExpenseAndTypeAndStatus(
                event.getExpense(), AlertType.ALL, AlertStatus.NEW));

        if (!existingAlerts.isEmpty()) {
            // Update the first alert found and delete any duplicates
            Alert alertToUpdate = existingAlerts.get(0);
            
            // Append the new violation to the existing message
            String combinedMessage = alertToUpdate.getMessage() + "; " + message;
            alertToUpdate.setMessage(combinedMessage);
            
            // Update type if necessary
            if (alertType == AlertType.DEPARTMENT && alertToUpdate.getType() == AlertType.CATEGORY) {
                alertToUpdate.setType(AlertType.ALL);
            }
            
            alertRepository.save(alertToUpdate);
            log.info("Updated existing alert with combined message: {}", alertToUpdate);
            
            // Delete any duplicate alerts (if more than one was found)
            for (int i = 1; i < existingAlerts.size(); i++) {
                alertRepository.delete(existingAlerts.get(i));
                log.info("Deleted duplicate alert: {}", existingAlerts.get(i));
            }
            return;
        }

        // Create a new alert if no existing alert was found
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

        // Determine the scope (category or department) and the appropriate name
        String scope;
        String scopeName;
        if (event.getBudgetType() == BudgetExceededEvent.BudgetType.DEPARTAMENT) {
            scope = "department";
            scopeName = event.getExpense().getUser().getDepartment().getName();
        } else {
            scope = "category";
            scopeName = event.getCategory().getName();
        }

        return String.format(
                "%s budget exceeded for %s '%s' on %s. " +
                "Current total: %s, New total: %s, Budget limit: %s",
                timeFrame,
                scope,
                scopeName,
                formattedTime,
                event.getCurrentTotal(),
                event.getNewTotal(),
                event.getBudgetLimit()
        );
    }
}
