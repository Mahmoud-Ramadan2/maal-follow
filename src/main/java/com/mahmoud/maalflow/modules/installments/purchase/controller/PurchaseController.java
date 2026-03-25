package com.mahmoud.maalflow.modules.installments.purchase.controller;


import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseRequest;
import com.mahmoud.maalflow.modules.installments.purchase.dto.PurchaseResponse;
import com.mahmoud.maalflow.modules.installments.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

    private final PurchaseService productService;

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.status(201).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseResponse> updatePurchase(@PathVariable Long id, @Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.status(200).body(productService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(productService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseResponse>> listPurchases(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String search) {

        return ResponseEntity.status(200).body(productService.list(page, size, search));
    }

    // TODO implement Soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> SoftDeletePurchase(@PathVariable Long id) {
        String message = productService.softDelete(id);
        return ResponseEntity.ok(message);
    }
}
