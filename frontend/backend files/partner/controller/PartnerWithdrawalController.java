package com.mahmoud.maalflow.modules.installments.partner.controller;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerWithdrawalRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerWithdrawalResponse;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for Partner Withdrawal management.
 * Handles withdrawal requests, approvals, and processing operations.
 *
 * @author Mahmoud
 */
@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/api/v1/partner-withdrawals")
public class PartnerWithdrawalController {

    private final PartnerWithdrawalService withdrawalService;

    /**
     * Create a new withdrawal request.
     */
    @PostMapping
    public ResponseEntity<PartnerWithdrawalResponse> createWithdrawalRequest(
            @Valid @RequestBody PartnerWithdrawalRequest request) {

        log.info("Creating withdrawal request for partner ID: {}, amount: {}",
                request.getPartnerId(), request.getAmount());

        PartnerWithdrawalResponse response = withdrawalService.createWithdrawalRequest(request);
        return ResponseEntity.created(URI.create("/api/v1/partner-withdrawals/" + response.getId()))
                .body(response);
    }

    /**
     * Get withdrawals by partner ID.
     */
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<PartnerWithdrawalResponse>> getWithdrawalsByPartner(
            @PathVariable Long partnerId) {

        log.info("Retrieving withdrawals for partner ID: {}", partnerId);

        List<PartnerWithdrawalResponse> responses = withdrawalService.getWithdrawalsByPartnerId(partnerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all pending withdrawals (for admin review).
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PartnerWithdrawalResponse>> getPendingWithdrawals() {

        log.info("Retrieving all pending withdrawals");

        List<PartnerWithdrawalResponse> responses = withdrawalService.getPendingWithdrawals();
        return ResponseEntity.ok(responses);
    }

    /**
     * Approve a withdrawal request.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<PartnerWithdrawalResponse> approveWithdrawal(@PathVariable Long id) {

        log.info("Approving withdrawal request ID: {}", id);

        PartnerWithdrawalResponse response = withdrawalService.approveWithdrawal(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Process an approved withdrawal (complete the transaction).
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<PartnerWithdrawalResponse> processWithdrawal(@PathVariable Long id) {

        log.info("Processing withdrawal ID: {}", id);

        PartnerWithdrawalResponse withdrawal = withdrawalService.processWithdrawal(id);
        return ResponseEntity.ok(withdrawal);
    }

    /**
     * Reject a withdrawal request.
     * Note: This endpoint would require a rejectWithdrawal method in the service.
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<String> rejectWithdrawal(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        log.info("Rejecting withdrawal request ID: {} with reason: {}", id, reason);
        // TODO: Implement rejectWithdrawal in the service
//      String result = withdrawalService.rejectWithdrawal(id, reason);

        return ResponseEntity.ok("Withdrawal rejection functionality to be implemented");
    }

    /**
     * Get withdrawal by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PartnerWithdrawalResponse> getWithdrawalById(@PathVariable Long id) {

        log.info("Retrieving withdrawal by ID: {}", id);

        PartnerWithdrawalResponse response = withdrawalService.getWithdrawalById(id);

        return ResponseEntity.ok(response);
    }
}
