package com.financialapp.finances.web.error;

import com.financialapp.finances.domain.exception.DomainException;
import com.financialapp.finances.domain.exception.ErrorCategory;
import com.financialapp.finances.web.dto.response.ApiResponse;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Maps exceptions thrown by the finances web controllers to the {@link ApiResponse} envelope.
 * Domain errors map by their {@link ErrorCategory}; bean-validation maps to 400; Feign call
 * failures pass their status through; anything else is a 500.
 */
@RestControllerAdvice(basePackages = "com.financialapp.finances.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        HttpStatus status = toStatus(ex.getError().getCategory());
        log.warn("Domain error [{}]: {}", ex.getError().getCode(), ex.getMessage());
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Not found / bad argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).toList();
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder().success(false).message("Validation failed").errors(errors).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeign(FeignException ex) {
        HttpStatus status = ex.status() > 0 ? HttpStatus.valueOf(ex.status()) : HttpStatus.BAD_GATEWAY;
        log.warn("Downstream call failed: status={}, message={}", ex.status(), ex.getMessage());
        return ResponseEntity.status(status).body(ApiResponse.error("Communication error between services"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }

    private HttpStatus toStatus(ErrorCategory category) {
        return switch (category) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
