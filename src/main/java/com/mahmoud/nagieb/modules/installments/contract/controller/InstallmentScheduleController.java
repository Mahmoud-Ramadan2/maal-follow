package com.mahmoud.nagieb.modules.installments.contract.controller;

import com.mahmoud.nagieb.modules.installments.contract.dto.InstallmentScheduleRequest;
import com.mahmoud.nagieb.modules.installments.contract.dto.InstallmentScheduleResponse;
import com.mahmoud.nagieb.modules.installments.contract.service.InstallmentScheduleService;
import com.mahmoud.nagieb.modules.installments.contract.enums.PaymentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing installment schedules.
 *
 * Supports:
 * - Auto-generation with rounded amounts (multiples of 50)
 * - Custom generation with number of months OR monthly amount
 * - Swapping remainder between first and last installment
 * - Rescheduling unpaid installments
 * - Payment tracking with discount support
 * - Skip month payment (e.g., Ramadan)
 *
 * @author Mahmoud
 */
@RestController
@RequestMapping("/api/v1/installment-schedules")
@RequiredArgsConstructor
public class InstallmentScheduleController {

    private final InstallmentScheduleService scheduleService;
    private final MessageSource messageSource;

    // ============== SCHEDULE GENERATION ENDPOINTS ==============

    /**
     * Auto-generate installment schedules for a contract (default settings)
     * Uses contract's default months, remainder goes to last installment
     *
     * POST /api/v1/installment-schedules/generate/{contractId}
     */
    @PostMapping("/generate/{contractId}")
    public ResponseEntity<List<InstallmentScheduleResponse>> generateSchedules(
            @PathVariable Long contractId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.generateSchedulesForContract(contractId));
    }

    /**
     * Generate installment schedules with custom parameters
     *
     * You can provide:
     * - numberOfMonths only: System calculates monthly amount
     * - monthlyAmount only: System calculates number of months
     * - Both: System validates they match
     * - putRemainderFirst: true to put remainder in first installment, false for last
     *
     * POST /api/v1/installment-schedules/generate/{contractId}/custom
     *
     * Examples:
     * 1. ?numberOfMonths=12 - Generate 12 installments, calculate amount
     * 2. ?monthlyAmount=500 - Use 500 per month, calculate number of months
     * 3. ?numberOfMonths=12&monthlyAmount=500 - Use both (must match)
     * 4. ?numberOfMonths=12&putRemainderFirst=true - Put remainder in first installment
     */
    @PostMapping("/generate/{contractId}/custom")
    public ResponseEntity<List<InstallmentScheduleResponse>> generateSchedulesCustom(
            @PathVariable Long contractId,
            @RequestParam(required = false) Integer numberOfMonths,
            @RequestParam(required = false) BigDecimal monthlyAmount,
            @RequestParam(defaultValue = "false") boolean putRemainderFirst) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.generateSchedules(contractId, numberOfMonths, monthlyAmount, putRemainderFirst));
    }

    /**
     * Swap remainder amount between first and last installment
     * Use this after generation to move the remainder to the other end
     *
     * Example: If you have [100, 50, 50, 50], swap to get [50, 50, 50, 100]
     *
     * PUT /api/v1/installment-schedules/swap-remainder/{contractId}
     */
    @PutMapping("/swap-remainder/{contractId}")
    public ResponseEntity<List<InstallmentScheduleResponse>> swapRemainderPosition(
            @PathVariable Long contractId) {

        return ResponseEntity.ok(scheduleService.swapRemainderPosition(contractId));
    }

    /**
     * Delete all unpaid schedules for a contract (for regeneration)
     *
     * DELETE /api/v1/installment-schedules/unpaid/{contractId}
     */
    @DeleteMapping("/unpaid/{contractId}")
    public ResponseEntity<Void> deleteUnpaidSchedules(@PathVariable Long contractId) {
        scheduleService.deleteUnpaidSchedules(contractId);
        return ResponseEntity.noContent().build();
    }

    // ============== RESCHEDULE ENDPOINTS ==============

    /**
     * Reschedule unpaid installments with new parameters
     *
     * PUT /api/v1/installment-schedules/reschedule/{contractId}
     *
     * Parameters:
     * - newNumberOfMonths: Number of months for new schedule (optional if monthlyAmount provided)
     * - newMonthlyAmount: Monthly amount for new schedule (optional if numberOfMonths provided)
     * - newStartDate: Start date for new schedule
     */
    @PutMapping("/reschedule/{contractId}")
    public ResponseEntity<List<InstallmentScheduleResponse>> rescheduleUnpaidInstallments(
            @PathVariable Long contractId,
            @RequestParam(required = false) Integer newNumberOfMonths,
            @RequestParam(required = false) BigDecimal newMonthlyAmount,
            @RequestParam(required = false) LocalDate newStartDate) {

        return ResponseEntity.ok(scheduleService.rescheduleUnpaidInstallments(
                contractId, newNumberOfMonths, newMonthlyAmount, newStartDate));
    }

    /**
     * Skip payment for a month (postpone all unpaid installments by 1 month)
     * Useful for Ramadan or special circumstances
     *
     * PUT /api/v1/installment-schedules/skip-month/{contractId}
     */
    @PutMapping("/skip-month/{contractId}")
    public ResponseEntity<Void> skipMonthPayment(
            @PathVariable Long contractId,
            @RequestParam String reason) {

        scheduleService.skipMonthPayment(contractId, reason);
        return ResponseEntity.ok().build();
    }

    // ============== CRUD ENDPOINTS ==============

    /**
     * Create a single installment schedule manually
     *
     * POST /api/v1/installment-schedules
     */
    @PostMapping
    public ResponseEntity<InstallmentScheduleResponse> createSchedule(
            @Valid @RequestBody InstallmentScheduleRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.createSchedule(request));
    }

    /**
     * Update an existing installment schedule
     *
     * PUT /api/v1/installment-schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<InstallmentScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody InstallmentScheduleRequest request) {

        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }


    // ============== QUERY ENDPOINTS ==============

    /**
     * Get all schedules for a contract
     *
     * GET /api/v1/installment-schedules/contract/{contractId}
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<InstallmentScheduleResponse>> getByContract(
            @PathVariable Long contractId) {

        return ResponseEntity.ok(scheduleService.getSchedulesByContractId(contractId));
    }

    /**
     * Get all overdue schedules
     *
     * GET /api/v1/installment-schedules/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<InstallmentScheduleResponse>> getOverdue() {
        return ResponseEntity.ok(scheduleService.getOverdueSchedules());
    }

    /**
     * Get schedules due soon (within N days)
     *
     * GET /api/v1/installment-schedules/due-soon?daysAhead=5
     */
    @GetMapping("/due-soon")
    public ResponseEntity<List<InstallmentScheduleResponse>> getDueSoon(
            @RequestParam(defaultValue = "5") int daysAhead) {

        return ResponseEntity.ok(scheduleService.getSchedulesDueSoon(daysAhead));
    }

    /**
     * Get schedules by payment day (for collection route optimization)
     *
     * GET /api/v1/installment-schedules/by-payment-day/{day}
     */
    @GetMapping("/by-payment-day/{day}")
    public ResponseEntity<List<InstallmentScheduleResponse>> getByPaymentDay(
            @PathVariable Integer day) {

        return ResponseEntity.ok(scheduleService.getSchedulesByPaymentDay(day));
    }

    /**
     * Get schedules by customer name
     *
     * GET /api/v1/installment-schedules/by-name?name=Ahmed
     */
    @GetMapping("/by-name")
    public ResponseEntity<List<InstallmentScheduleResponse>> getByName(
            @RequestParam String name) {

        return ResponseEntity.ok(scheduleService.getSchedulesByName(name));
    }

    /**
     * Get schedules by status (paginated)
     *
     * GET /api/v1/installment-schedules/by-status/PENDING?page=0&size=20
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<Page<InstallmentScheduleResponse>> getByStatus(
            @PathVariable PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(scheduleService.getSchedulesByStatus(status, PageRequest.of(page, size)));
    }

    /**
     * Get monthly collection summary
     *
     * GET /api/v1/installment-schedules/monthly-summary?month=2026-01
     */
    @GetMapping("/monthly-summary")
    public ResponseEntity<InstallmentScheduleService.MonthlyCollectionSummary> getMonthlySummary(
            @RequestParam String month) {

        return ResponseEntity.ok(scheduleService.getMonthlyCollectionSummary(month));
    }
}

