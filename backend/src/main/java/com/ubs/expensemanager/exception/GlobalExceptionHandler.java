package com.ubs.expensemanager.exception;

import com.ubs.expensemanager.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

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
         * Handles resource not found errors (e.g. entity not found).
         */
    @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(
                ResourceNotFoundException ex,
                HttpServletRequest request
        ) {
        return buildErrorResponse(
                HttpServletResponse.SC_NOT_FOUND, // 404
                "Not Found",
                ex.getMessage(),
                request.getServletPath(),
                null
        );
        }

        /**
         * Handles business conflicts (e.g. duplicated department name).
         */
    @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponse> handleConflict(
                ConflictException ex,
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
         * Handles access denied errors when user lacks required permissions.
         */
    @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                AccessDeniedException ex,
                HttpServletRequest request
        ) {
        return buildErrorResponse(
                HttpServletResponse.SC_FORBIDDEN, // 403
                "Access denied",
                "You do not have permission to perform this action",
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
