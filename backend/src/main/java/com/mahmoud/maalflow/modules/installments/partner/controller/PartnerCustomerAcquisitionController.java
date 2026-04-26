package com.mahmoud.maalflow.modules.installments.partner.controller;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCustomerAcquisitionRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCustomerAcquisitionResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCustomerAcquisition;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerCustomerAcquisitionMapper;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerCustomerAcquisitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for partner customer acquisition management.
 * Implements requirement: "إضافة عملاء يعملون بالأقساط لحسابي ولهم نسبة"
 */
@RestController
@RequestMapping("/api/v1/partner-customer-acquisitions")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*", maxAge = 3600)
public class PartnerCustomerAcquisitionController {

    private final PartnerCustomerAcquisitionService acquisitionService;
    private final PartnerCustomerAcquisitionMapper acquisitionMapper;

    /**
     * Assign a customer to a partner.
     */
    @PostMapping
    public ResponseEntity<PartnerCustomerAcquisitionResponse> assignCustomerToPartner(
            @Valid @RequestBody PartnerCustomerAcquisitionRequest request) {
        log.info("REST request to assign customer {} to partner {}", request.getCustomerId(), request.getPartnerId());

        PartnerCustomerAcquisition acquisition = acquisitionService.assignCustomerToPartner(
                request.getPartnerId(),
                request.getCustomerId(),
                request.getCommissionPercentage(),
                request.getAcquisitionNotes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(acquisitionMapper.toResponse(acquisition));
    }

    /**
     * Get all customers assigned to a partner.
     */
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<PartnerCustomerAcquisitionResponse>> getPartnerCustomers(@PathVariable Long partnerId) {
        log.info("REST request to get customers for partner: {}", partnerId);
        List<PartnerCustomerAcquisition> acquisitions = acquisitionService.getPartnerCustomers(partnerId);
        List<PartnerCustomerAcquisitionResponse> responses = acquisitions.stream()
                .map(acquisitionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Transfer customer from one partner to another.
     */
    @PutMapping("/transfer")
    public ResponseEntity<Void> transferCustomer(
            @RequestParam Long customerId,
            @RequestParam Long fromPartnerId,
            @RequestParam Long toPartnerId,
            @RequestParam(required = false) String reason) {
        log.info("REST request to transfer customer {} from partner {} to partner {}",
                customerId, fromPartnerId, toPartnerId);

        acquisitionService.transferCustomer(customerId, fromPartnerId, toPartnerId, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * Get partner performance metrics.
     */
    @GetMapping("/partner/{partnerId}/performance")
    public ResponseEntity<PartnerCustomerAcquisitionService.PartnerPerformanceMetrics> getPartnerPerformance(
            @PathVariable Long partnerId) {
        log.info("REST request to get performance metrics for partner: {}", partnerId);
        var metrics = acquisitionService.getPartnerPerformance(partnerId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Update commission earned for a partner-customer relationship.
     */
    @PutMapping("/commission")
    public ResponseEntity<Void> updateCommissionEarned(
            @RequestParam Long partnerId,
            @RequestParam Long customerId,
            @RequestParam BigDecimal commissionAmount) {
        log.info("REST request to update commission for partner {} customer {}: {}",
                partnerId, customerId, commissionAmount);

        acquisitionService.updateCommissionEarned(partnerId, customerId, commissionAmount);
        return ResponseEntity.ok().build();
    }
}
