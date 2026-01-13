package com.mahmoud.nagieb.modules.installments.contract.controller;


import com.mahmoud.nagieb.modules.installments.contract.dto.ContractRequest;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractResponse;
import com.mahmoud.nagieb.modules.installments.contract.service.ContractService;
import com.mahmoud.nagieb.modules.shared.enums.ContractStatus;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
@AllArgsConstructor
public class ContractController {

    private final ContractService contractService;

//    public ContractController (ContractService contractService) {
//        this.contractService = contractService;
//    }

    @PostMapping
    public ResponseEntity<ContractResponse> createContract(
            @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(201).body(contractService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.ok(contractService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getContract(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getById(id));
    }

    @GetMapping("/contract-number/{contractNumber}")
    public ResponseEntity<ContractResponse> getByContractNumber(@PathVariable String contractNumber) {
        return ResponseEntity.ok(contractService.getByContractNumber(contractNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<ContractResponse>> getCustomerContracts(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contractService.getCustomerWithContracts(customerId, page, size));
    }

    @GetMapping("/customer/{customerId}/all-linked")
    public ResponseEntity<List<ContractResponse>> getAllLinkedCustomerContracts(@PathVariable Long customerId) {
        return ResponseEntity.ok(contractService.getAllContractsForLinkedCustomers(customerId));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<Page<ContractResponse>> getByStatus(
            @PathVariable ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contractService.getContractsByStatus(status, page, size));
    }

    @GetMapping("/by-payment-day/{day}")
    public ResponseEntity<List<ContractResponse>> getByPaymentDay(@PathVariable Integer day) {
        return ResponseEntity.ok(contractService.getContractsByPaymentDay(day));
    }

    @GetMapping("/by-address")
    public ResponseEntity<List<ContractResponse>> getByAddress(@RequestParam String address) {
        return ResponseEntity.ok(contractService.getContractsByAddress(address));
    }

    @GetMapping("/total-monthly-expected")
    public ResponseEntity<BigDecimal> getTotalMonthlyExpected() {
        return ResponseEntity.ok(contractService.getTotalMonthlyExpected());
    }

    @GetMapping("/total-net-profit")
    public ResponseEntity<BigDecimal> getTotalNetProfit() {
        return ResponseEntity.ok(contractService.getTotalNetProfit());
    }

    @GetMapping("/{id}/early-payment-discount")
    public ResponseEntity<BigDecimal> calculateEarlyPaymentDiscount(
            @PathVariable Long id,
            @RequestParam BigDecimal remainingAmount) {
        return ResponseEntity.ok(contractService.calculateEarlyPaymentDiscount(id, remainingAmount));
    }

    @GetMapping("/{id}/cash-discount")
    public ResponseEntity<BigDecimal> calculateCashDiscount(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.calculateCashDiscount(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ContractResponse> markAsCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.markAsCompleted(id));
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
//        contractService.deleteContract(id);
//        return ResponseEntity.noContent().build();
//    }
}
