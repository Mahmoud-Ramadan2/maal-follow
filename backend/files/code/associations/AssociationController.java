package com.mahmoud.maalflow.modules.associations.controller;

import com.mahmoud.maalflow.modules.associations.dto.*;
import com.mahmoud.maalflow.modules.associations.entity.AssociationPayment;
import com.mahmoud.maalflow.modules.associations.service.AssociationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Associations (جمعيات).
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/associations/controller/
 */
@RestController
@RequestMapping("/api/v1/associations")
@RequiredArgsConstructor
public class AssociationController {

    private final AssociationService service;

    @PostMapping
    public ResponseEntity<AssociationResponse> create(@Valid @RequestBody AssociationRequest request) {
        return ResponseEntity.status(201).body(service.createAssociation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssociationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<AssociationResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(page, size));
    }

    @PostMapping("/members")
    public ResponseEntity<AssociationMemberResponse> addMember(@Valid @RequestBody AssociationMemberRequest request) {
        return ResponseEntity.status(201).body(service.addMember(request));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<AssociationMemberResponse>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMembers(id));
    }

    @PostMapping("/payments")
    public ResponseEntity<Void> recordPayment(@Valid @RequestBody AssociationPaymentRequest request) {
        service.recordPayment(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/payments/{month}")
    public ResponseEntity<List<AssociationPayment>> getPayments(@PathVariable Long id, @PathVariable String month) {
        return ResponseEntity.ok(service.getPaymentsForMonth(id, month));
    }

    @PutMapping("/members/{memberId}/received")
    public ResponseEntity<Void> markReceived(@PathVariable Long memberId) {
        service.markMemberReceived(memberId);
        return ResponseEntity.ok().build();
    }
}

