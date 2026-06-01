package com.financialapp.finances.web.error;

import com.financialapp.finances.domain.exception.DomainException;
import com.financialapp.finances.domain.exception.transaction.UnownedTransactionException;
import com.financialapp.finances.web.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain exceptions thrown by the new transaction controllers to the web ApiResponse envelope.
 * Scoped to the web package and ordered ahead of the legacy GlobalExceptionHandler so the new
 * controllers get DDD-aware mappings while legacy controllers keep their handler.
 */
@RestControllerAdvice(basePackages = "com.financialapp.finances.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DomainExceptionHandler {

    @ExceptionHandler(UnownedTransactionException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnowned(UnownedTransactionException ex) {
        log.warn("Unowned transaction: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundOrBadArg(IllegalArgumentException ex) {
        log.warn("Bad request / not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        log.warn("Domain rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
