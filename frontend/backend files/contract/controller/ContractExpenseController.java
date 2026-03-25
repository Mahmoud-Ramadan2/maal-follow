package com.mahmoud.maalflow.modules.installments.contract.controller;

import com.mahmoud.maalflow.modules.installments.contract.dto.ContractExpenseRequest;
import com.mahmoud.maalflow.modules.installments.contract.dto.ContractExpenseResponse;
import com.mahmoud.maalflow.modules.installments.contract.service.ContractExpenseService;
import com.mahmoud.maalflow.modules.shared.enums.ExpenseType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contract-expenses")
@RequiredArgsConstructor
public class ContractExpenseController {

    private final ContractExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ContractExpenseResponse> createExpense(
            @Valid @RequestBody ContractExpenseRequest request)
 {
        return ResponseEntity.status(201).body(expenseService.createExpense(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ContractExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id ,request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractExpenseResponse> getExpense(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<ContractExpenseResponse>> getByContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(expenseService.getExpensesByContractId(contractId));
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<ContractExpenseResponse>> getByType(@PathVariable ExpenseType type) {
        return ResponseEntity.ok(expenseService.getExpensesByType(type));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ContractExpenseResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(startDate, endDate));
    }

    @GetMapping("/contract/{contractId}/total")
    public ResponseEntity<BigDecimal> getTotalForContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(expenseService.getTotalExpensesForContract(contractId));
    }

    @GetMapping("/partner/{partnerId}/total")
    public ResponseEntity<BigDecimal> getTotalForPartner(@PathVariable Long partnerId) {
        return ResponseEntity.ok(expenseService.getTotalExpensesForPartner(partnerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}