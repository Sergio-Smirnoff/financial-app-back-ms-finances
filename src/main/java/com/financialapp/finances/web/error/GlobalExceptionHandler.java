package com.financialapp.finances.web.error;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.error.ApiExceptionHandler;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.financialapp.finances.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler extends ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Not found / bad argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
                HttpStatus.NOT_FOUND, "resource_not_found", ex.getMessage(), null));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeign(FeignException ex) {
        HttpStatus status = ex.status() > 0 ? HttpStatus.valueOf(ex.status()) : HttpStatus.BAD_GATEWAY;
        log.warn("Downstream call failed: status={}, message={}", ex.status(), ex.getMessage());
        return ResponseEntity.status(status).body(ApiResponse.failure(
                status, "downstream_error", "Communication error between services", null));
    }
}
