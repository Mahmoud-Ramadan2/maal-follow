package com.mahmoud.maalflow.modules.installments.partner.controller;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerResponse;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Partner management.
 */
@RestController
@RequestMapping("/api/v1/partners")
@AllArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @PostMapping
    public ResponseEntity<PartnerResponse> createPartner(@Valid @RequestBody PartnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerService.createPartner(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponse> getPartnerById(@PathVariable Long id) {
        PartnerResponse response = partnerService.getPartnerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PartnerResponse>> getAllPartners(
            @RequestParam(required = false) PartnerStatus status) {
        List<PartnerResponse> partnerResponses;
        if (status != null) {
            partnerResponses = partnerService.getPartnersByStatus(status);
        } else {
            partnerResponses = partnerService.getAllPartners();
        }
        return ResponseEntity.ok(partnerResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartnerResponse> updatePartner(
            @PathVariable Long id,
           @Valid @RequestBody PartnerRequest request) {
        PartnerResponse response = partnerService.updatePartner(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return ResponseEntity.noContent().build();
    }
}

