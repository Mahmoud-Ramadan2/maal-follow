package com.mahmoud.nagieb.modules.installments.ledger.controller;

import com.mahmoud.nagieb.modules.installments.ledger.dto.*;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerType;
import com.mahmoud.nagieb.modules.installments.ledger.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Daily Ledger management.
 * Implements idempotent ledger entry creation for duplicate prevention.
 *
 * @author Mahmoud
 */
@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    /**
     * Creates a new ledger entry with idempotency support.
     * If the same idempotency key is used, returns the existing entry.
     */
    @PostMapping
    public ResponseEntity<LedgerResponse> createLedgerEntry(@Valid @RequestBody LedgerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerService.createLedgerEntry(request));
    }

    /**
     * Gets ledger entry by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LedgerResponse> getEntryById(@PathVariable Long id) {
        return ResponseEntity.ok(ledgerService.getById(id));
    }

    /**
     * Gets ledger entry by idempotency key.
     */
    @GetMapping("/by-idempotency-key/{idempotencyKey}")
    public ResponseEntity<LedgerResponse> getEntryByIdempotencyKey(@PathVariable String idempotencyKey) {
        return ledgerService.getByIdempotencyKey(idempotencyKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing ledger entry.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LedgerResponse> updateEntry(
            @PathVariable Long id,
            @Valid @RequestBody LedgerRequest request) {
        return ResponseEntity.ok(ledgerService.update(id, request));
    }

    /**
     * Lists all ledger entries with pagination and optional search.
     */
    @GetMapping
    public ResponseEntity<Page<LedgerSummary>> listEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ledgerService.list(page, size, search));
    }

    /**
     * Lists ledger entries by date.
     */
    @GetMapping("/by-date/{date}")
    public ResponseEntity<Page<LedgerSummary>> listByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ledgerService.listByDate(date, page, size));
    }

    /**
     * Lists ledger entries by type (INCOME or EXPENSE).
     */
    @GetMapping("/by-type/{type}")
    public ResponseEntity<Page<LedgerSummary>> listByType(
            @PathVariable LedgerType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ledgerService.listByType(type, page, size));
    }

    /**
     * Lists ledger entries by source (COLLECTION, PURCHASE, OTHER).
     */
    @GetMapping("/by-source/{source}")
    public ResponseEntity<Page<LedgerSummary>> listBySource(
            @PathVariable LedgerSource source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ledgerService.listBySource(source, page, size));
    }

    /**
     * Lists ledger entries within a date range.
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<Page<LedgerSummary>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ledgerService.listByDateRange(startDate, endDate, page, size));
    }

    /**
     * Lists ledger entries by partner.
     */
    @GetMapping("/by-partner/{partnerId}")
    public ResponseEntity<Page<LedgerSummary>> listByPartner(
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ledgerService.listByPartner(partnerId, page, size));
    }

    /**
     * Gets daily ledger summary for a date range.
     */
    @GetMapping("/daily-summary")
    public ResponseEntity<List<DailyLedgerSummary>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ledgerService.getDailySummary(startDate, endDate));
    }

    /**
     * Gets ledger statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<LedgerStatistics> getStatistics() {
        return ResponseEntity.ok(ledgerService.getStatistics());
    }

    /**
     * Deletes a ledger entry.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEntry(@PathVariable Long id) {
        String message = ledgerService.delete(id);
        return ResponseEntity.ok(message);
    }
}

