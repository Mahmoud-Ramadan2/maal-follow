package com.mahmoud.maalflow.modules.installments.capital.controller;


import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionRequest;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionResponse;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalTransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for capital transaction management.
 *
 * @author Mahmoud
 */
@RestController
@RequestMapping("/api/v1/partner/capital-transactions")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CapitalTransactionController {

    private final CapitalTransactionService capitalTransactionService;

    /**
     * Create a new capital transaction.
     */
    @PostMapping
    public ResponseEntity<CapitalTransactionResponse> createCapitalTransaction(@Valid @RequestBody CapitalTransactionRequest request) {
        log.info("REST request to create capital transaction: type={}, amount={}",
                request.getTransactionType(), request.getAmount());
        CapitalTransactionResponse response = capitalTransactionService.createCapitalTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all capital transactions with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<CapitalTransactionResponse>> getAllCapitalTransactions(Pageable pageable) {
        log.info("REST request to get all capital transactions");
        Page<CapitalTransactionResponse> response = capitalTransactionService.getAllCapitalTransactions(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get capital transactions by type.
     */
    @GetMapping("/type/{transactionType}")
    public ResponseEntity<List<CapitalTransactionResponse>> getCapitalTransactionsByType(
            @PathVariable CapitalTransactionType transactionType) {
        log.info("REST request to get capital transactions by type: {}", transactionType);
        List<CapitalTransactionResponse> response = capitalTransactionService.getCapitalTransactionsByType(transactionType);
        return ResponseEntity.ok(response);
    }

    /**
     * Get capital transactions for a specific partner.
     */
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<CapitalTransactionResponse>> getPartnerCapitalTransactions(
            @PathVariable Long partnerId) {
        log.info("REST request to get capital transactions for partner: {}", partnerId);
        List<CapitalTransactionResponse> response = capitalTransactionService.getPartnerCapitalTransactions(partnerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get capital transactions within date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<CapitalTransactionResponse>> getCapitalTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("REST request to get capital transactions between {} and {}", startDate, endDate);
        List<CapitalTransactionResponse> response = capitalTransactionService.getCapitalTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly capital transaction summary.
     */
    @GetMapping("/summary/monthly/{year}/{month}")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlySummary(
            @PathVariable int year,
            @PathVariable int month) {
        log.info("REST request to get monthly summary for {}-{}", year, month);
        Map<String, BigDecimal> response = capitalTransactionService.getMonthlySummary(year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Get partner capital summary for a date range.
     */
    @GetMapping("/summary/partner/{partnerId}")
    public ResponseEntity<Map<String, BigDecimal>> getPartnerCapitalSummary(
            @PathVariable Long partnerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("REST request to get partner capital summary for partner: {} between {} and {}",
                partnerId, startDate, endDate);
        Map<String, BigDecimal> response = capitalTransactionService.getPartnerCapitalSummary(partnerId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
