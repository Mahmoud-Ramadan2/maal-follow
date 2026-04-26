package com.mahmoud.maalflow.modules.installments.payment.entity;

import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentProcessingStatus;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment entity representing actual payments received from customers.
 * Includes idempotency key for preventing duplicate payments.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Idempotency key to prevent duplicate payment processing.
     * Should be unique per payment attempt.
     */
    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_schedule_id")
    private InstallmentSchedule installmentSchedule;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentProcessingStatus status = PaymentProcessingStatus.PENDING;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @NotNull
    @Column(name = "actual_payment_date", nullable = false)
    private LocalDate actualPaymentDate;

    @Size(max = 7)
    @NotNull
    @Column(name = "agreed_payment_month", nullable = false, length = 7)
    private String agreedPaymentMonth;

    @ColumnDefault("0")
    @Column(name = "is_early_payment")
    private Boolean isEarlyPayment = false;

    @ColumnDefault("0.00")
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "received_by", nullable = false)
    private User receivedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id")
    private User collector;

    @Column(name = "receipt_document_id")
    private Long receiptDocumentId;


    /**
     * Pre-persist hook to calculate net amount if not set.
     */
    @PrePersist
    public void prePersist() {
        if (this.netAmount == null) {
            this.netAmount = this.amount.subtract(
                    this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO
            );
        }
        if (this.status == null) {
            this.status = PaymentProcessingStatus.PENDING;
        }
    }
}
