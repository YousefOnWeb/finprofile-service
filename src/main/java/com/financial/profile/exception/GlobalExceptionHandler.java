package com.financial.profile.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.financial.profile.dto.ApiResponse;

/**
 * Centralized exception handler for all controllers.
 *
 * <p>Analogy: this is the complaint-resolution desk. No matter which
 * receptionist (controller) encounters a problem, every issue is translated
 * here into a consistent {@link ApiResponse} envelope with the right HTTP
 * status code. Customers (API callers) always get a predictable shape.</p>
 *
 * <p>{@code @RestControllerAdvice} means this advice applies across all
 * {@code @RestController} classes automatically.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Jakarta Validation failures on request bodies.
     *
     * <p>When {@code @Valid} finds invalid fields, Spring throws
     * {@link MethodArgumentNotValidException}. We extract each field error
     * and list them so the caller knows exactly what to fix.</p>
     *
     * @param ex the validation exception
     * @return 400 Bad Request with field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Invalid value",
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("Validation failed: " + errors.size()
                        + " field(s) invalid"));
    }

    /**
     * Handle "resource not found" situations.
     *
     * @param ex the not-found exception
     * @return 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handle business-rule violations (e.g., duplicate email).
     *
     * @param ex the illegal argument exception
     * @return 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handle any unexpected exception as a last resort.
     *
     * <p>We log nothing here intentionally in this milestone; in production
     * you would log the full stack trace server-side and return only a safe
     * generic message to the client.</p>
     *
     * @param ex the unexpected exception
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("An unexpected error occurred."));
    }
}
