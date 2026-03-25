package com.mahmoud.maalflow.modules.installments.vendor.controller;

import com.mahmoud.maalflow.modules.installments.vendor.dto.VendorRequest;
import com.mahmoud.maalflow.modules.installments.vendor.dto.VendorResponse;
import com.mahmoud.maalflow.modules.installments.vendor.dto.VendorSummary;
import com.mahmoud.maalflow.modules.installments.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors")
@AllArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<VendorSummary> createVendor(@Valid @RequestBody VendorRequest request) {
        return ResponseEntity.status(201).body(vendorService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VendorSummary> updateVendor(@PathVariable Long id, @Valid @RequestBody VendorRequest request) {

        return ResponseEntity.ok(vendorService.update(id, request));
    }


    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getById(id));
    }
    @GetMapping
    public ResponseEntity<Page<VendorSummary>> list(Pageable pageable,
                                                    @RequestParam(required = false) String search,
                                                    @RequestParam(defaultValue = "active", required = false) String status) {
        Page<VendorSummary> vendorSummaries = vendorService.list(pageable, search, status);
        return ResponseEntity.ok(vendorSummaries);
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<String> delete(@PathVariable Long id) {
        String message =vendorService.softDelete(id);
        return ResponseEntity.ok(message);
    }
}

