package com.mahmoud.maalflow.modules.installments.profit.controller;

import com.mahmoud.maalflow.modules.installments.profit.dto.MonthlyProfitDistributionRequest;
import com.mahmoud.maalflow.modules.installments.profit.dto.MonthlyProfitDistributionResponse;
import com.mahmoud.maalflow.modules.installments.profit.dto.ProfitDistributionLifecycleStatusResponse;
import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import com.mahmoud.maalflow.modules.installments.profit.service.MonthlyProfitDistributionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Monthly Profit Distribution management.
 */
@RestController
@RequestMapping("/api/v1/profit-distributions")
@RequiredArgsConstructor
@Validated
public class MonthlyProfitDistributionController {

    private final MonthlyProfitDistributionService service;

    @PostMapping
    public ResponseEntity<MonthlyProfitDistributionResponse> createDistribution(
            @Valid @RequestBody MonthlyProfitDistributionRequest request) {
        MonthlyProfitDistribution created = service.createDistribution(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonthlyProfitDistributionResponse> getDistributionById(@PathVariable Long id) {
        MonthlyProfitDistribution distribution = service.getDistributionById(id);
        return ResponseEntity.ok(toResponse(distribution));
    }

    @GetMapping("/month/{monthYear}")
    public ResponseEntity<MonthlyProfitDistributionResponse> getDistributionByMonth(
            @PathVariable @Pattern(regexp = "^\\d{4}-\\d{2}$") String monthYear) {
        MonthlyProfitDistribution distribution = service.getDistributionByMonth(monthYear);
        return ResponseEntity.ok(toResponse(distribution));
    }

    @GetMapping
    public ResponseEntity<List<MonthlyProfitDistributionResponse>> getAllDistributions(
            @RequestParam(required = false) ProfitDistributionStatus status,
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}$") String startMonth,
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}$") String endMonth) {

        List<MonthlyProfitDistribution> distributions;

        if (status != null) {
            distributions = service.getDistributionsByStatus(status);
        } else if (startMonth != null && endMonth != null) {
            distributions = service.getDistributionsByDateRange(startMonth, endMonth);
        } else {
            distributions = service.getAllDistributions();
        }

        return ResponseEntity.ok(distributions.stream().map(this::toResponse).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonthlyProfitDistributionResponse> updateDistribution(
            @PathVariable Long id,
            @Valid @RequestBody MonthlyProfitDistributionRequest request) {
        MonthlyProfitDistribution updated = service.updateDistribution(id, request);
        return ResponseEntity.ok(toResponse(updated));
    }

//    @PostMapping("/{id}/calculate")
//    public ResponseEntity<MonthlyProfitDistributionResponse> calculateProfit(@PathVariable Long id) {
//        MonthlyProfitDistribution calculated = service.calculateProfit(id);
//        return ResponseEntity.ok(toResponse(calculated));
//    }



    @PostMapping("/{id}/distribute")
    public ResponseEntity<MonthlyProfitDistributionResponse> distributeProfit(@PathVariable Long id) {
        MonthlyProfitDistribution distributed = service.distributeProfit(id);
        return ResponseEntity.ok(toResponse(distributed));
    }

    @PostMapping("/{id}/undo-distribute")
    public ResponseEntity<MonthlyProfitDistributionResponse> undoDistributeProfit(@PathVariable Long id) {
        MonthlyProfitDistribution undone = service.undoDistribution(id);
        return ResponseEntity.ok(toResponse(undone));
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<MonthlyProfitDistributionResponse> lockDistribution(@PathVariable Long id) {
        MonthlyProfitDistribution locked = service.lockDistribution(id);
        return ResponseEntity.ok(toResponse(locked));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ProfitDistributionLifecycleStatusResponse> getDistributionStatus(@PathVariable Long id) {
        MonthlyProfitDistribution distribution = service.getDistributionById(id);
        ProfitDistributionLifecycleStatusResponse response = ProfitDistributionLifecycleStatusResponse.builder()
                .distributionId(distribution.getId())
                .monthYear(distribution.getMonthYear())
                .status(distribution.getStatus())
                .build();
        return ResponseEntity.ok(response);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteDistribution(@PathVariable Long id) {
//        service.deleteDistribution(id);
//        return ResponseEntity.noContent().build();
//    }

    private MonthlyProfitDistributionResponse toResponse(MonthlyProfitDistribution distribution) {
        return MonthlyProfitDistributionResponse.builder()
                .id(distribution.getId())
                .monthYear(distribution.getMonthYear())
                .totalProfit(distribution.getTotalProfit())
                .managementFeePercentage(distribution.getManagementFeePercentage())
                .zakatPercentage(distribution.getZakatPercentage())
                .managementFeeAmount(distribution.getManagementFeeAmount())
                .zakatAmount(distribution.getZakatAmount())
                .contractExpensesAmount(distribution.getContractExpensesAmount())
                .distributableProfit(distribution.getDistributableProfit())
                .ownerProfit(distribution.getOwnerProfit())
                .partnersTotalProfit(distribution.getPartnersTotalProfit())
                .status(distribution.getStatus())
                .calculationNotes(distribution.getCalculationNotes())
                .createdAt(distribution.getCreatedAt())
                .updatedAt(distribution.getUpdatedAt())
                .build();
    }
}

