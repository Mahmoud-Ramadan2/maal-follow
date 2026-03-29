package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.payment.dto.DailyPaymentSummary;
import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentStatistics;
import com.mahmoud.maalflow.modules.installments.payment.repo.PaymentRepository;
import com.mahmoud.maalflow.modules.installments.schedule.repo.InstallmentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for payment statistics and reporting.
 * Implements requirement 12: "حساب مجموع الأقساط المفترض ورودها شهريا والأقساط الواردة فعليا"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentStatisticsService {

    private final PaymentRepository paymentRepository;
    private final InstallmentScheduleRepository scheduleRepository;

    /**
     * Calculate monthly payment statistics.
     * Implements: "حساب مجموع الأقساط المفترض ورودها شهريا والأقساط الواردة فعليا"
     */
    @Transactional(readOnly = true)
    public PaymentStatistics calculateMonthlyStatistics(YearMonth month) {
        log.info("Calculating payment statistics for month: {}", month);

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        // Calculate expected payments for the month
        BigDecimal expectedPayments = scheduleRepository.calculateExpectedPaymentsForMonth(
                startDate, endDate);

        // Calculate actual payments received
        BigDecimal actualPayments = paymentRepository.sumPaymentsByAgreedMonth(month.toString());

        // Calculate collection efficiency
        BigDecimal collectionRate = BigDecimal.ZERO;
        if (expectedPayments.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = actualPayments
                    .divide(expectedPayments, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Calculate other statistics
        BigDecimal overduePaid = paymentRepository.sumOverduePaymentsForMonth(month.toString());
        BigDecimal earlyPayments = paymentRepository.sumEarlyPaymentsForMonth(month.toString());
        BigDecimal totalDiscounts = paymentRepository.sumDiscountsForMonth(month.toString());

        int totalPaymentCount = paymentRepository.countPaymentsByAgreedMonth(month.toString());
        int earlyPaymentCount = paymentRepository.countEarlyPaymentsByMonth(month.toString());

        PaymentStatistics statistics = PaymentStatistics.builder()
                .month(month.toString())
                .expectedPayments(expectedPayments != null ? expectedPayments : BigDecimal.ZERO)
                .actualPayments(actualPayments != null ? actualPayments : BigDecimal.ZERO)
                .collectionRate(collectionRate)
                .overduePaid(overduePaid != null ? overduePaid : BigDecimal.ZERO)
                .earlyPayments(earlyPayments != null ? earlyPayments : BigDecimal.ZERO)
                .totalDiscounts(totalDiscounts != null ? totalDiscounts : BigDecimal.ZERO)
                .totalPaymentCount(totalPaymentCount)
                .earlyPaymentCount(earlyPaymentCount)
                .shortfall(expectedPayments.subtract(actualPayments))
                .build();

        log.info("Monthly statistics calculated - Expected: {}, Actual: {}, Collection Rate: {}%",
                expectedPayments, actualPayments, collectionRate);

        return statistics;
    }

    /**
     * Get daily payment summaries for a month.
     */
    @Transactional(readOnly = true)
    public List<DailyPaymentSummary> getDailyPaymentSummaries(YearMonth month) {
        log.info("Getting daily payment summaries for month: {}", month);

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        return getDailyPaymentSummaries(startDate, endDate, paymentRepository);
    }

    static List<DailyPaymentSummary> getDailyPaymentSummaries(LocalDate startDate, LocalDate endDate, PaymentRepository paymentRepository) {
        List<Object[]> results = paymentRepository.getDailyPaymentSummaries(startDate, endDate);
        return results.stream()
                .map(row -> {
                    DailyPaymentSummary summary = new DailyPaymentSummary();
                    summary.setPaymentDate((LocalDate) row[0]);
                    summary.setPaymentCount(((Long) row[1]).intValue());
                    summary.setTotalAmount((BigDecimal) row[2]);
                    return summary;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate year-to-date statistics.
     */
    @Transactional(readOnly = true)
    public PaymentStatistics calculateYearToDateStatistics(int year) {
        log.info("Calculating year-to-date payment statistics for year: {}", year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // Calculate totals for the year
        BigDecimal expectedPayments = scheduleRepository.calculateExpectedPaymentsForPeriod(
                startDate, endDate);
        BigDecimal actualPayments = paymentRepository.sumPaymentsForYear(year);

        // Calculate collection rate
        BigDecimal collectionRate = BigDecimal.ZERO;
        if (expectedPayments.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = actualPayments
                    .divide(expectedPayments, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Other year statistics
        BigDecimal totalDiscounts = paymentRepository.sumDiscountsForYear(year);
        int totalPaymentCount = paymentRepository.countPaymentsForYear(year);

        return PaymentStatistics.builder()
                .month(String.valueOf(year))
                .expectedPayments(expectedPayments != null ? expectedPayments : BigDecimal.ZERO)
                .actualPayments(actualPayments != null ? actualPayments : BigDecimal.ZERO)
                .collectionRate(collectionRate)
                .totalDiscounts(totalDiscounts != null ? totalDiscounts : BigDecimal.ZERO)
                .totalPaymentCount(totalPaymentCount)
                .shortfall(expectedPayments.subtract(actualPayments))
                .build();
    }

    /**
     * Get payment performance by customer.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCustomerPaymentPerformance(YearMonth month) {
        log.info("Getting customer payment performance for month: {}", month);
        return paymentRepository.getCustomerPaymentPerformance(month.toString());
    }

    /**
     * Get overdue payment summary.
     */
    @Transactional(readOnly = true)
    public PaymentStatistics getOverduePaymentSummary() {
        log.info("Calculating overdue payment summary");

        LocalDate today = LocalDate.now();

        BigDecimal totalOverdue = scheduleRepository.calculateOverdueAmount(today);
        int overdueCount = scheduleRepository.countOverdueInstallments(today);

        return PaymentStatistics.builder()
                .month("OVERDUE")
                .expectedPayments(totalOverdue != null ? totalOverdue : BigDecimal.ZERO)
                .actualPayments(BigDecimal.ZERO)
                .collectionRate(BigDecimal.ZERO)
                .totalPaymentCount(overdueCount)
                .shortfall(totalOverdue != null ? totalOverdue : BigDecimal.ZERO)
                .build();
    }
}
