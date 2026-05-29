package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentReportResponse;
import com.mahmoud.maalflow.modules.installments.payment.entity.Payment;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentProcessingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentReportService {

    private final PaymentQueryService paymentQueryService;

    public PaymentReportResponse getReportSummary(
            String month,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isEarlyPayment,
            PaymentProcessingStatus status,
            PaymentMethod paymentMethod,
            Long collectorId,
            Long contractId,
            String customerName,
            BigDecimal minNetAmount
    ) {
        List<Payment> payments = paymentQueryService.findAllByFilters(
                month,
                startDate,
                endDate,
                isEarlyPayment,
                status,
                paymentMethod,
                collectorId,
                contractId,
                customerName,
                minNetAmount
        );

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalNetAmount = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        int earlyCount = 0;
        int completedCount = 0;
        int cancelledCount = 0;
        int refundedCount = 0;

        for (Payment payment : payments) {
            totalAmount = totalAmount.add(payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO);
            totalNetAmount = totalNetAmount.add(payment.getNetAmount() != null ? payment.getNetAmount() : BigDecimal.ZERO);
            totalDiscounts = totalDiscounts.add(payment.getDiscountAmount() != null ? payment.getDiscountAmount() : BigDecimal.ZERO);

            if (Boolean.TRUE.equals(payment.getIsEarlyPayment())) {
                earlyCount++;
            }
            if (payment.getStatus() == PaymentProcessingStatus.COMPLETED) {
                completedCount++;
            }
            if (payment.getStatus() == PaymentProcessingStatus.CANCELLED) {
                cancelledCount++;
            }
            if (payment.getStatus() == PaymentProcessingStatus.REFUNDED) {
                refundedCount++;
            }
        }

        return PaymentReportResponse.builder()
                .month(month)
                .startDate(startDate)
                .endDate(endDate)
                .totalCount(payments.size())
                .completedCount(completedCount)
                .cancelledCount(cancelledCount)
                .refundedCount(refundedCount)
                .earlyPaymentCount(earlyCount)
                .totalAmount(totalAmount)
                .totalNetAmount(totalNetAmount)
                .totalDiscounts(totalDiscounts)
                .build();
    }
}

