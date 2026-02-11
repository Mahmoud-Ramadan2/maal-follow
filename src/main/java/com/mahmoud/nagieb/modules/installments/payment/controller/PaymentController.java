package com.mahmoud.nagieb.modules.installments.payment.controller;

import com.mahmoud.nagieb.modules.installments.payment.dto.*;
import com.mahmoud.nagieb.modules.installments.payment.enums.DiscountType;
import com.mahmoud.nagieb.modules.installments.payment.service.PaymentDiscountService;
import com.mahmoud.nagieb.modules.installments.payment.service.PaymentReminderService;
import com.mahmoud.nagieb.modules.installments.payment.service.PaymentService;
import com.mahmoud.nagieb.modules.installments.payment.service.PaymentStatisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced Payment Controller with comprehensive payment management.
 * Implements all Arabic requirements for payment processing.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentDiscountService discountService;
    private final PaymentReminderService reminderService;
    private final PaymentStatisticsService statisticsService;

    /**
     * Process a new payment with automatic discount calculation.
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("REST request to process payment for amount: {}", request.getAmount());
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get payment by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.info("REST request to get payment: {}", id);
        PaymentResponse response = paymentService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by idempotency key.
     */
    @GetMapping("/by-key/{idempotencyKey}")
    public ResponseEntity<PaymentResponse> getPaymentByIdempotencyKey(@PathVariable String idempotencyKey) {
        log.info("REST request to get payment by idempotency key: {}", idempotencyKey);
        Optional<PaymentResponse> response = paymentService.getByIdempotencyKey(idempotencyKey);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List all payments with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<PaymentSummary>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get all payments - page: {}, size: {}", page, size);
        Page<PaymentSummary> response = paymentService.list(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payments by month.
     */
    @GetMapping("/month/{month}")
    public ResponseEntity<Page<PaymentSummary>> getPaymentsByMonth(
            @PathVariable String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get payments for month: {}", month);
        Page<PaymentSummary> response = paymentService.listByMonth(month, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payments within date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<PaymentSummary>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get payments between {} and {}", startDate, endDate);
        Page<PaymentSummary> response = paymentService.listByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get early payments.
     */
    @GetMapping("/early-payments")
    public ResponseEntity<Page<PaymentSummary>> getEarlyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get early payments");
        Page<PaymentSummary> response = paymentService.listEarlyPayments(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Calculate early payment discount.
     */
    @GetMapping("/calculate-discount/early")
    public ResponseEntity<BigDecimal> calculateEarlyPaymentDiscount(
            @RequestParam BigDecimal amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        log.info("REST request to calculate early payment discount");
        BigDecimal discount = discountService.calculateEarlyPaymentDiscount(amount, paymentDate, dueDate);
        return ResponseEntity.ok(discount);
    }

    /**
     * Calculate final installment discount.
     */
    @GetMapping("/calculate-discount/final")
    public ResponseEntity<BigDecimal> calculateFinalInstallmentDiscount(
            @RequestParam BigDecimal amount,
            @RequestParam boolean isFinalInstallment) {
        log.info("REST request to calculate final installment discount");
        BigDecimal discount = discountService.calculateFinalInstallmentDiscount(amount, isFinalInstallment);
        return ResponseEntity.ok(discount);
    }

    /**
     * Get monthly payment statistics.
     */
    @GetMapping("/statistics/monthly/{year}/{month}")
    public ResponseEntity<PaymentStatistics> getMonthlyStatistics(
            @PathVariable int year,
            @PathVariable int month) {
        log.info("REST request to get monthly payment statistics for {}-{}", year, month);
        YearMonth yearMonth = YearMonth.of(year, month);
        PaymentStatistics statistics = statisticsService.calculateMonthlyStatistics(yearMonth);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get year-to-date payment statistics.
     */
    @GetMapping("/statistics/ytd/{year}")
    public ResponseEntity<PaymentStatistics> getYearToDateStatistics(@PathVariable int year) {
        log.info("REST request to get year-to-date payment statistics for {}", year);
        PaymentStatistics statistics = statisticsService.calculateYearToDateStatistics(year);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get overdue payment summary.
     */
    @GetMapping("/statistics/overdue")
    public ResponseEntity<PaymentStatistics> getOverduePaymentSummary() {
        log.info("REST request to get overdue payment summary");
        PaymentStatistics statistics = statisticsService.getOverduePaymentSummary();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get daily payment summaries for a month.
     */
    @GetMapping("/statistics/daily/{year}/{month}")
    public ResponseEntity<List<DailyPaymentSummary>> getDailyPaymentSummaries(
            @PathVariable int year,
            @PathVariable int month) {
        log.info("REST request to get daily payment summaries for {}-{}", year, month);
        YearMonth yearMonth = YearMonth.of(year, month);
        List<DailyPaymentSummary> summaries = statisticsService.getDailyPaymentSummaries(yearMonth);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Create payment reminders for upcoming due dates.
    @PostMapping("/reminders/create")
    public ResponseEntity<Void> createPaymentReminders() {
        log.info("REST request to create payment reminders");
        reminderService.createPaymentReminders();
        return ResponseEntity.ok().build();
    }

    /**
     * Send pending payment reminders.
    @PostMapping("/reminders/send")
    public ResponseEntity<Void> sendPendingReminders() {
        log.info("REST request to send pending payment reminders");
        reminderService.sendPendingReminders();
        return ResponseEntity.ok().build();
    }

    /**
     * Cancel a payment.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        log.info("REST request to cancel payment: {}", id);
        PaymentResponse response = paymentService.cancelPayment(id, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Refund a payment.
     */
    @PutMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        log.info("REST request to refund payment: {}", id);
        PaymentResponse response = paymentService.refundPayment(id, reason);
        return ResponseEntity.ok(response);
    }

    // ================ Payment Discount Configuration Endpoints ================

    /**
     * Create or update payment discount configuration.
    @PostMapping("/discount-config")
    public ResponseEntity<PaymentDiscountConfigResponse> saveDiscountConfig(
            @Valid @RequestBody PaymentDiscountConfigRequest request) {
        log.info("REST request to save discount configuration: {}", request.getDiscountType());
        PaymentDiscountConfigResponse response = discountService.saveDiscountConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get active discount configuration by type.
     */
    @GetMapping("/discount-config/{discountType}")
    public ResponseEntity<PaymentDiscountConfigResponse> getDiscountConfig(@PathVariable DiscountType discountType) {
        log.info("REST request to get discount configuration for type: {}", discountType);
        PaymentDiscountConfigResponse response = discountService.getDiscountConfig(discountType);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active discount configurations.
     */
    @GetMapping("/discount-config")
    public ResponseEntity<List<PaymentDiscountConfigResponse>> getAllActiveDiscountConfigs() {
        log.info("REST request to get all active discount configurations");
        List<PaymentDiscountConfigResponse> response = discountService.getAllActiveDiscountConfigs();
        return ResponseEntity.ok(response);
    }

    /**
     * Activate discount configuration.
     */

    @PutMapping("/discount-config/{id}/activate")
    public ResponseEntity<PaymentDiscountConfigResponse> activateDiscountConfig(@PathVariable Long  id) {
        log.info("REST request to activate discount configuration for type: {}", id);

        return ResponseEntity.ok(discountService.activateDiscountConfig(id));
    }

    /**
     * Deactivate discount configuration.
     */
    @DeleteMapping("/discount-config/{discountType}")
    public ResponseEntity<Void> deactivateDiscountConfig(@PathVariable DiscountType discountType) {
        log.info("REST request to deactivate discount configuration for type: {}", discountType);
        discountService.deactivateDiscountConfig(discountType);
        return ResponseEntity.noContent().build();
    }
}
