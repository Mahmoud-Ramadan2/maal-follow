package com.mahmoud.maalflow.modules.installments.capital.controller;

import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolRequest;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolResponse;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalPoolService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for capital pool management.
 */
@RestController
@RequestMapping("/api/v1/capital-pool")
@AllArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*", maxAge = 3600)
public class CapitalPoolController {

    private final CapitalPoolService capitalPoolService;

    /**
     * Get current capital pool status.
     */
    @GetMapping("/current")
    public ResponseEntity<CapitalPoolResponse> getCurrentCapitalPool() {
        log.info("REST request to get current capital pool");
        CapitalPoolResponse response = capitalPoolService.getCurrentCapitalPool();
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new capital pool entry.
     */
    @PostMapping
    public ResponseEntity<CapitalPoolResponse> createCapitalPool(@Valid @RequestBody CapitalPoolRequest request) {
        log.info("REST request to create capital pool with total amount: {}", request.getTotalAmount());
        CapitalPoolResponse response = capitalPoolService.createCapitalPool(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update capital pool with new amounts.
     */
    @PutMapping
    public ResponseEntity<CapitalPoolResponse> updateCapitalPool(@Valid @RequestBody CapitalPoolRequest request) {
        log.info("REST request to update capital pool");
        CapitalPoolResponse response = capitalPoolService.updateCapitalPool(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Recalculate capital pool from transactions.
     */
    @PostMapping("/recalculate")
    public ResponseEntity<CapitalPoolResponse> recalculateCapitalPool() {
        log.info("REST request to recalculate capital pool");
        CapitalPoolResponse response = capitalPoolService.recalculateCapitalPool();
        return ResponseEntity.ok(response);
    }

    /**
     * Get capital pool history.
     */
    @GetMapping("/history")
    public ResponseEntity<Page<CapitalPoolResponse>> getCapitalPoolHistory(Pageable pageable) {
        log.info("REST request to get capital pool history");
        Page<CapitalPoolResponse> response = capitalPoolService.getCapitalPoolHistory(pageable);
        return ResponseEntity.ok(response);
    }
}
