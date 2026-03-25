package com.mahmoud.maalflow.modules.installments.profit.controller;

import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import com.mahmoud.maalflow.modules.installments.profit.service.MonthlyProfitDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Monthly Profit Distribution management.
 */
@RestController
@RequestMapping("/api/v1/profit-distributions")
@RequiredArgsConstructor
public class MonthlyProfitDistributionController {

    private final MonthlyProfitDistributionService service;

    @PostMapping
    public ResponseEntity<MonthlyProfitDistribution> createDistribution(
            @RequestBody MonthlyProfitDistribution distribution) {
        MonthlyProfitDistribution created = service.createDistribution(distribution);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonthlyProfitDistribution> getDistributionById(@PathVariable Long id) {
        MonthlyProfitDistribution distribution = service.getDistributionById(id);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/month/{monthYear}")
    public ResponseEntity<MonthlyProfitDistribution> getDistributionByMonth(
            @PathVariable String monthYear) {
        MonthlyProfitDistribution distribution = service.getDistributionByMonth(monthYear);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping
    public ResponseEntity<List<MonthlyProfitDistribution>> getAllDistributions(
            @RequestParam(required = false) ProfitDistributionStatus status,
            @RequestParam(required = false) String startMonth,
            @RequestParam(required = false) String endMonth) {

        List<MonthlyProfitDistribution> distributions;

        if (status != null) {
            distributions = service.getDistributionsByStatus(status);
        } else if (startMonth != null && endMonth != null) {
            distributions = service.getDistributionsByDateRange(startMonth, endMonth);
        } else {
            distributions = service.getAllDistributions();
        }

        return ResponseEntity.ok(distributions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonthlyProfitDistribution> updateDistribution(
            @PathVariable Long id,
            @RequestBody MonthlyProfitDistribution distribution) {
        MonthlyProfitDistribution updated = service.updateDistribution(id, distribution);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<MonthlyProfitDistribution> calculateProfit(@PathVariable Long id) {
        MonthlyProfitDistribution calculated = service.calculateProfit(id);
        return ResponseEntity.ok(calculated);
    }

    @PostMapping("/{id}/distribute")
    public ResponseEntity<MonthlyProfitDistribution> distributeProfit(@PathVariable Long id) {
        MonthlyProfitDistribution distributed = service.distributeProfit(id);
        return ResponseEntity.ok(distributed);
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<MonthlyProfitDistribution> lockDistribution(@PathVariable Long id) {
        MonthlyProfitDistribution locked = service.lockDistribution(id);
        return ResponseEntity.ok(locked);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDistribution(@PathVariable Long id) {
        service.deleteDistribution(id);
        return ResponseEntity.noContent().build();
    }
}

