package com.mahmoud.maalflow.modules.installments.partner.controller;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerMonthlyProfitAdjustRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerMonthlyProfitPayRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerMonthlyProfitResponse;
import com.mahmoud.maalflow.modules.installments.partner.dto.PayoutReconciliationResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerMonthlyProfitMapper;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerMonthlyProfitService;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerPayoutReconciliationService;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerProfitCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partner-monthly-profits")
@RequiredArgsConstructor
@Validated
public class PartnerMonthlyProfitController {

    private final PartnerMonthlyProfitService partnerMonthlyProfitService;
    private final PartnerProfitCalculationService partnerProfitCalculationService;
    private final PartnerPayoutReconciliationService partnerPayoutReconciliationService;
    private final PartnerMonthlyProfitMapper partnerMonthlyProfitMapper;

    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<PartnerMonthlyProfitResponse>> getByPartnerId(@PathVariable Long partnerId) {
        List<PartnerMonthlyProfitResponse> response = partnerMonthlyProfitService.getByPartnerId(partnerId)
                .stream()
                .map(partnerMonthlyProfitMapper::toPartnerMonthlyProfitResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/distribution/{distributionId}")
    public ResponseEntity<List<PartnerMonthlyProfitResponse>> getByDistributionId(@PathVariable Long distributionId) {
        List<PartnerMonthlyProfitResponse> response = partnerMonthlyProfitService.getByDistributionId(distributionId)
                .stream()
                .map(partnerMonthlyProfitMapper::toPartnerMonthlyProfitResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PartnerMonthlyProfitResponse> pay(@PathVariable Long id,
                                                            @Valid @RequestBody PartnerMonthlyProfitPayRequest request) {
        PartnerMonthlyProfit paid = partnerMonthlyProfitService.payMonthlyProfit(
                id,
                request.getPaidByUserId(),
                request.getPaymentMethod(),
                request.getPaymentDate(),
                request.getNotes()
        );
        return ResponseEntity.ok(partnerMonthlyProfitMapper.toPartnerMonthlyProfitResponse(paid));
    }

    @PostMapping("/{id}/reinvest")
    public ResponseEntity<PartnerMonthlyProfitResponse> reinvest(@PathVariable Long id,
                                                            @Valid @RequestBody PartnerMonthlyProfitPayRequest request) {
        PartnerMonthlyProfit reinvest = partnerMonthlyProfitService.reinvestMonthlyProfit(
                id,
                request.getPaidByUserId(),
                request.getPaymentMethod(),
                request.getPaymentDate(),
                request.getNotes()
        );
        return ResponseEntity.ok(partnerMonthlyProfitMapper.toPartnerMonthlyProfitResponse(reinvest));
    }

//    @PostMapping("/{id}/adjust")
//    public ResponseEntity<PartnerMonthlyProfitResponse> adjust(@PathVariable Long id,
//                                                               @Valid @RequestBody PartnerMonthlyProfitAdjustRequest request) {
//        partnerProfitCalculationService.adjustPartnerProfit(id, request.getNewAmount(), request.getReason());
//        PartnerMonthlyProfit adjusted = partnerMonthlyProfitService.getById(id);
//        return ResponseEntity.ok(partnerMonthlyProfitMapper.toPartnerMonthlyProfitResponse(adjusted));
//    }

    @GetMapping("/{id}/reconciliation")
    public ResponseEntity<PayoutReconciliationResponse> getMonthlyProfitReconciliation(@PathVariable Long id) {
        return ResponseEntity.ok(partnerPayoutReconciliationService.reconcileMonthlyProfitPayout(id));
    }
}

