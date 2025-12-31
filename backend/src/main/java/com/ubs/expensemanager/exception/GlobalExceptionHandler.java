package com.ubs.expensemanager.exception;

import com.ubs.expensemanager.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
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
     * Handles user registration conflicts when email already exists
     * @param ex the thrown Exception
     * @param request the HTTP request that triggered the exception
     * @return a {@link ErrorResponse} with status 409 (Conflict)
     */
    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(
            UserExistsException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpServletResponse.SC_CONFLICT, // 409
                "Conflict",
                ex.getMessage(),
                request.getServletPath(),
                null
        );
    }

    /**
     * Handles invalid enum values in request parameters.
     *
     * <p>Triggered when a request parameter cannot be converted to the required enum type.
     * Returns a user-friendly error message listing all valid enum values.</p>
     *
     * @param ex the type mismatch exception containing details about the conversion failure
     * @param request the HTTP request that triggered the exception
     * @return a {@link ErrorResponse} with status 400 (Bad Request) and valid enum values
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String paramName = ex.getName();
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";

        String message;
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            Object[] enumValues = ex.getRequiredType().getEnumConstants();
            String validValues = Arrays.stream(enumValues)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            message = String.format(
                    "Invalid value '%s' for parameter '%s'. Valid values are: %s",
                    invalidValue,
                    paramName,
                    validValues
            );
        } else {
            message = String.format(
                    "Invalid value '%s' for parameter '%s'",
                    invalidValue,
                    paramName
            );
        }

        return buildErrorResponse(
                HttpServletResponse.SC_BAD_REQUEST,
                "Bad Request",
                message,
                request.getServletPath(),
                null
        );
    }

    /**
     * Handles business rule violations in user and manager validation
     * @param ex the thrown Exception
     * @param request the HTTP request that triggered the exception
     * @return a {@link ErrorResponse} with status 400 (Bad Request)
     */
    @ExceptionHandler({
            ResourceNotFoundException.class,
            ManagerRequiredException.class,
            InvalidManagerRoleException.class,
            SelfManagerException.class,
            ManagerHasSubordinatesException.class,
            UserAlreadyActiveException.class
    })
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpServletResponse.SC_BAD_REQUEST,
                "Business rule violation",
                ex.getMessage(),
                request.getServletPath(),
                null
        );
    }

    /**
     * Handles authentication failures due to invalid credentials.
     *
     * <p>
     * Returns a 401 Unauthorized response with a generic message to avoid
     * exposing which part of the credentials was incorrect.
     * </p>
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid credentials",
                "Email or password is incorrect",
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
                        fieldError -> getCustomErrorMessage(fieldError, ex.getBindingResult().getTarget()),
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

    /**
     * Generates a custom error message for the FieldError
     * with type mismatch for enums and bools
     *
     * @param fieldError the field error
     * @param target binding obj
     * @return custom msg or default
     */
    private String getCustomErrorMessage(FieldError fieldError, Object target) {
        String msg = fieldError.getDefaultMessage();

        if (!fieldError.contains(TypeMismatchException.class)) {
            return msg;
        }

        if (target == null) {
            return msg;
        }

        String fieldName = fieldError.getField();
        Object invalidValue = fieldError.getRejectedValue();
        String strValue = invalidValue != null ? invalidValue.toString() : "null";

        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            Class<?> type = f.getType();

            if (type.isEnum()) {
                Object[] enumValues = type.getEnumConstants();
                String validValues = Arrays.stream(enumValues)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                return String.format(
                        "Invalid value '%s' for field '%s'. Valid values are: %s",
                        strValue,
                        fieldName,
                        validValues
                );
            } else if (type == Boolean.class || type == boolean.class) {
                return String.format(
                        "Invalid value '%s' for field '%s'. Valid values are: true, false",
                        strValue,
                        fieldName
                );
            }
        } catch (NoSuchFieldException e) {
            log.debug("Field not found during reflection: {}", fieldName);
        }

        return msg;
    }
}
