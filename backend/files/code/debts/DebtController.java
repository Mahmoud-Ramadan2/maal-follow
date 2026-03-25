package com.mahmoud.maalflow.modules.debts.controller;

import com.mahmoud.maalflow.modules.debts.dto.*;
import com.mahmoud.maalflow.modules.debts.entity.DebtPayment;
import com.mahmoud.maalflow.modules.debts.enums.DebtStatus;
import com.mahmoud.maalflow.modules.debts.enums.DebtType;
import com.mahmoud.maalflow.modules.debts.service.DebtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Debts (الديون).
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/debts/controller/
 */
@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService service;

    @PostMapping
    public ResponseEntity<DebtResponse> create(@Valid @RequestBody DebtRequest request) {
        return ResponseEntity.status(201).body(service.createDebt(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebtResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DebtResponse>> list(
            @RequestParam(required = false) DebtType type,
            @RequestParam(required = false) DebtStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(type, status, search, page, size));
    }

    @PostMapping("/payments")
    public ResponseEntity<DebtResponse> recordPayment(@Valid @RequestBody DebtPaymentRequest request) {
        return ResponseEntity.ok(service.recordPayment(request));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<DebtPayment>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPayments(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getSummary() {
        return ResponseEntity.ok(Map.of(
                "totalReceivables", service.getTotalReceivables(),
                "totalPayables", service.getTotalPayables(),
                "netPosition", service.getTotalReceivables().subtract(service.getTotalPayables())
        ));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancelDebt(id);
        return ResponseEntity.ok().build();
    }
}

