package com.mahmoud.maalflow.modules.installments.partner.controller;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCommissionRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCommissionResponse;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerCommissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for partner commission management.
 *
 * @author Mahmoud
 */
@RestController
@RequestMapping("/api/v1/partner/commissions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PartnerCommissionController {

    private final PartnerCommissionService commissionService;

    /**
     * Create a new partner commission.
     */
    @PostMapping
    public ResponseEntity<PartnerCommissionResponse> createCommission(@Valid @RequestBody PartnerCommissionRequest request) {
        log.info("REST request to create partner commission for partner: {}", request.getPartnerId());
        PartnerCommissionResponse response = commissionService.createCommission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get commissions by partner ID.
     */
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<Page<PartnerCommissionResponse>> getCommissionsByPartnerId(@PathVariable Long partnerId
    , @RequestParam(defaultValue = "0") int page
    , @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to get commissions for partner: {}", partnerId);
        Page<PartnerCommissionResponse> response = commissionService.getCommissionsByPartner(partnerId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get commissions by contract ID.
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<PartnerCommissionResponse>> getCommissionsByContractId(@PathVariable Long contractId) {
        log.info("REST request to get commissions for contract: {}", contractId);
        List<PartnerCommissionResponse> response = commissionService.getCommissionsByContractId(contractId);
        return ResponseEntity.ok(response);
    }

    /**
     * Approve a commission payment.
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<PartnerCommissionResponse> approveCommission(
            @PathVariable Long id,
            @RequestParam Long approvedByUserId) {
        log.info("REST request to approve commission: {} by user: {}", id, approvedByUserId);
        PartnerCommissionResponse response = commissionService.approveCommission(id, approvedByUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Pay a commission.
     */
    @PutMapping("/{id}/pay")
    public ResponseEntity<PartnerCommissionResponse> payCommission(@PathVariable Long id) {
        log.info("REST request to pay commission: {}", id);
        PartnerCommissionResponse response = commissionService.payCommission(id);
        return ResponseEntity.ok(response);
    }
}
