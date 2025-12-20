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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
