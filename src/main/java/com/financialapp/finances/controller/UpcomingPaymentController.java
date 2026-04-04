package com.financialapp.finances.controller;

import com.financialapp.finances.model.dto.response.ApiResponse;
import com.financialapp.finances.model.dto.response.UpcomingPaymentResponse;
import com.financialapp.finances.service.UpcomingPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finances/upcoming-payments")
@RequiredArgsConstructor
@Tag(name = "Upcoming Payments", description = "Unified view of loan and card expense payments due in a period")
public class UpcomingPaymentController {

    private final UpcomingPaymentService upcomingPaymentService;

    @GetMapping
    @Operation(summary = "Get upcoming payments",
               description = "Returns loan installments and card expense payments due between from and to dates, sorted by due date")
    public ResponseEntity<ApiResponse<List<UpcomingPaymentResponse>>> getUpcomingPayments(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(ApiResponse.ok(
                upcomingPaymentService.getUpcomingPayments(userId, from, to, currency)));
    }
}
