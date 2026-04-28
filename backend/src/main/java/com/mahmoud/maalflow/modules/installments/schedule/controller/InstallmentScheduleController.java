package com.mahmoud.maalflow.modules.installments.schedule.controller;

import com.mahmoud.maalflow.modules.installments.schedule.dto.InstallmentScheduleRequest;
import com.mahmoud.maalflow.modules.installments.schedule.dto.InstallmentScheduleResponse;
import com.mahmoud.maalflow.modules.installments.schedule.dto.MonthlyCollectionSummary;
import com.mahmoud.maalflow.modules.installments.schedule.dto.ScheduleMetadataUpdateRequest;
import com.mahmoud.maalflow.modules.installments.schedule.service.InstallmentScheduleService;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
     * Example: If you have [100, 50, 50, 50], swap to get [50, 50, 50, 100]
     */
    @PutMapping("/swap-remainder/{contractId}")
    public ResponseEntity<List<InstallmentScheduleResponse>> swapRemainderPosition(
            @PathVariable Long contractId) {

        return ResponseEntity.ok(scheduleService.swapRemainderPosition(contractId));
    }

    /**
     * Delete all unpaidby-name schedules for a contract (for regeneration)
     *
     * DELETE /api/v1/installment-schedules/unpaid/{contractId}
     */
//    @DeleteMapping("/unpaid/{contractId}")
//    public ResponseEntity<Void> deleteUnpaidSchedules(@PathVariable Long contractId) {
//        scheduleService.deleteUnpaidSchedules(contractId);
//        return ResponseEntity.noContent().build();
//    }

    // ============== RESCHEDULE ENDPOINTS ==============

    /**
     * Reschedule unpaid installments with new parameters
     *
     * PUT /api/v1/installment-schedules/reschedule/{contractId}
     *
     * Parameters:
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
     * Create a single installment collection manually
     *
     * POST /api/v1/installment-schedules
     */
//    @PostMapping
//    public ResponseEntity<InstallmentScheduleResponse> createSchedule(
//            @Valid @RequestBody InstallmentScheduleRequest request) {
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(scheduleService.createSchedule(request));
//    }


    @PatchMapping("/{id}/metadata")
    public ResponseEntity<InstallmentScheduleResponse> updateScheduleMetadata(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleMetadataUpdateRequest request) {
        return ResponseEntity.ok(scheduleService.updateScheduleMetadata(id, request));
    }    @GetMapping("/{id}")
    public ResponseEntity<InstallmentScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    // ============== QUERY ENDPOINTS ==============

    /**
     * Get all schedules for a contract
     *
     * GET /api/v1/installment-schedules/contract/{contractId}
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<Page<InstallmentScheduleResponse>> getByContract(
            Pageable pageable,
            @PathVariable Long contractId) {

        return ResponseEntity.ok(scheduleService.getSchedulesByContractId(pageable, contractId));
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
     * Unified multi-filter search endpoint.
     *
     * GET /api/v1/installment-schedules/search
     *
     * Supported optional filters:
     * - contractId
     * - status
     * - name (customer name)
     * - paymentDay
     * - startDate / endDate (dueDate range)
     * - overdueOnly
     * - dueSoonDays
     */
    @GetMapping("/search")
    public ResponseEntity<Page<InstallmentScheduleResponse>> search(
            Pageable pageable,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer paymentDay,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean overdueOnly,
            @RequestParam(required = false) Integer dueSoonDays
    ) {
        return ResponseEntity.ok(scheduleService.searchSchedules(
                pageable,
                contractId,
                status,
                name,
                paymentDay,
                startDate,
                endDate,
                overdueOnly,
                dueSoonDays
        ));
    }

    /**
     * Get monthly collection summary
     *
     * GET /api/v1/installment-schedules/monthly-summary?month=2026-01
     */
    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlyCollectionSummary> getMonthlySummary(
            @RequestParam String month) {

        return ResponseEntity.ok(scheduleService.getMonthlyCollectionSummary(month));
    }
}

