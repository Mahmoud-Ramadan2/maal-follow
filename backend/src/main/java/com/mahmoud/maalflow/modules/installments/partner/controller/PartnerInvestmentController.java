package com.mahmoud.maalflow.modules.installments.partner.controller;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentResponse;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerInvestmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/partner-investments")
public class PartnerInvestmentController {

    private final PartnerInvestmentService investmentService;

    @PostMapping
    public ResponseEntity<PartnerInvestmentResponse> createInvestment(
            @Valid @RequestBody PartnerInvestmentRequest request) {

        PartnerInvestmentResponse response = investmentService.createInvestment(request);
        return ResponseEntity.created(URI.create("/api/v1/partner-investments/" + response.getId()))
                .body(response);
    }

    @GetMapping("/{partnerId}/by-partner")
    public ResponseEntity<List<PartnerInvestmentResponse>> getInvestmentsByPartner(@PathVariable Long partnerId) {
        List<PartnerInvestmentResponse> responses = investmentService.getInvestmentsByPartnerId(partnerId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerInvestmentResponse> getInvestmentById(@PathVariable Long id) {
        PartnerInvestmentResponse response = investmentService.getInvestmentResponseById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<PartnerInvestmentResponse> confirmInvestment(@PathVariable Long id) {
        PartnerInvestmentResponse response = investmentService.confirmInvestment(id);
        return ResponseEntity.ok(response);
    }

}
