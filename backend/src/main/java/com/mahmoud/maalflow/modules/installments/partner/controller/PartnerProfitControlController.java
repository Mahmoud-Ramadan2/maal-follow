package com.mahmoud.maalflow.modules.installments.partner.controller;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerProfitConfigRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerProfitConfigResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerProfitCalculationConfig;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerProfitControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/partner-profit-controls")
@RequiredArgsConstructor
@Validated
public class PartnerProfitControlController {

    private final PartnerProfitControlService profitControlService;

    @GetMapping("/current")
    public ResponseEntity<PartnerProfitConfigResponse> getCurrentConfig() {
        PartnerProfitCalculationConfig config = profitControlService.getCurrentConfig();
        return ResponseEntity.ok(toResponse(config));
    }

    @PutMapping
    public ResponseEntity<Void> updateConfig(@Valid @RequestBody PartnerProfitConfigRequest request) {
        profitControlService.updateProfitCalculationConfig(
                request.getManagementFeePercentage(),
                request.getZakatPercentage(),
                request.getProfitPaymentDay()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{partnerId}/sharing/start")
    public ResponseEntity<Void> startProfitSharing(@PathVariable Long partnerId,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        profitControlService.startProfitSharing(partnerId, startDate);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{partnerId}/sharing/pause")
    public ResponseEntity<Void> pauseProfitSharing(@PathVariable Long partnerId,
                                                   @RequestParam(required = false) String reason) {
        profitControlService.pauseProfitSharing(partnerId, reason != null ? reason : "No reason provided");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{partnerId}/sharing/resume")
    public ResponseEntity<Void> resumeProfitSharing(@PathVariable Long partnerId) {
        profitControlService.resumeProfitSharing(partnerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{partnerId}/sharing/eligibility")
    public ResponseEntity<Boolean> isEligible(@PathVariable Long partnerId,
                                              @RequestParam("month") String month) {
        return ResponseEntity.ok(profitControlService.isPartnerEligibleForProfit(partnerId, month));
    }

    private PartnerProfitConfigResponse toResponse(PartnerProfitCalculationConfig config) {
        return PartnerProfitConfigResponse.builder()
                .id(config.getId())
                .managementFeePercentage(config.getManagementFeePercentage())
                .zakatPercentage(config.getZakatPercentage())
                .profitPaymentDay(config.getProfitPaymentDay())
                .newPartnerDelayMonths(config.getNewPartnerDelayMonths())
                .active(config.getIsActive())
                .notes(config.getNotes())
                .build();
    }
}

