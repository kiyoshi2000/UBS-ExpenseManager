package com.ubs.expensemanager.exception;

import com.ubs.expensemanager.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for the application
 *
 * <p>Handles authentication failures, validation errors and uncaught
 * exceptions, returning {@link ErrorResponse} objects with appropriate
 * HTTP Status code.</p>
 *
 * <p>All Exceptions are logged for debugging and auditing purposes.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles auth failures caused by invalid credentials (bad email/password)
     * @param ex the thrown Exception
     * @param request the HTTP request that triggered the exception
     * @return a {@link ErrorResponse} with status 401
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                ex.getMessage(),
                request.getServletPath(),
                null
        );
    }

    /**
     * Handles uncaught exceptions
     * @param ex the thrown Exception
     * @param request the HTTP request that triggered the exception
     * @return a {@link ErrorResponse} with status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage(),
                request.getServletPath(),
                null
        );
    }

    /**
     * Handles validation errors on method arguments
     *
     * <p>Returns a structured ErrorResponse including field-specific messages
     * for all validation failures.</p>
     *
     * @param ex the thrown Exception
     * @param request the HTTP request that triggered the exception
     * @return a {@link ErrorResponse} with status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        return buildErrorResponse(
                HttpServletResponse.SC_BAD_REQUEST,
                "Validation error",
                "Validation failed for one or more fields",
                request.getServletPath(),
                fieldErrors
        );
    }

    /**
     * Builds the {@link ErrorResponse} and logs the error
     * @param status HTTP status code
     * @param error short error message description
     * @param message detailed error message
     * @param path the endpoint path that triggered the exception
     * @param fieldErrors the list of invalid fields (nullable)
     * @return {@link ResponseEntity} containing the ErrorResponse
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        log.warn("Exception[{}]: {}: {} at {}", status, error, message, path);

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                fieldErrors
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}
