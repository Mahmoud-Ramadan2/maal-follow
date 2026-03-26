package com.mahmoud.maalflow.modules.installments.contract.entity;

import com.mahmoud.maalflow.modules.installments.contract.listener.InstallmentListener;
import com.mahmoud.maalflow.modules.installments.payment.entity.Payment;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entity representing individual payment schedules for installment contracts.
 * Each schedule represents one monthly payment with profit tracking.
 *
 * @author Mahmoud
 */
@Entity
@EntityListeners(InstallmentListener.class)
@Table(name = "installment_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber = 0;

    @NotNull
    @Column(name = "profit_month", nullable = false, length = 7)
    private String profitMonth; // Format: YYYY-MM

    @Column(name = "discount_applied", precision = 12, scale = 2)
    private BigDecimal discountApplied = BigDecimal.ZERO;

    @Column(name = "is_final_payment")
    private Boolean isFinalPayment = false;

    @NotNull
    @Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalAmount = BigDecimal.ZERO;

    @Column(name = "principal_amount", precision = 12, scale = 2)
    private BigDecimal principalAmount = BigDecimal.ZERO;

    @Column(name = "profit_amount", precision = 12, scale = 2)
    private BigDecimal profitAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "principal_paid", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal principalPaid = BigDecimal.ZERO;

    @Column(name = "profit_paid", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal profitPaid = BigDecimal.ZERO;

    @Column(name = "notes" , columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id")
    @ToString.Exclude
    private User collector;



    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "installment_contract_id", nullable = false)
    @ToString.Exclude
    private Contract contract;

    @OneToMany(mappedBy = "installmentSchedule")
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "installmentSchedule")
    private Set<ContractExpense> contractExpenses = new LinkedHashSet<>();

    @Column(name = "created_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
        if (this.isFinalPayment == null) {
            this.isFinalPayment = false;
        }
        if (this.discountApplied == null) {
            this.discountApplied = BigDecimal.ZERO;
        }
        if (this.originalAmount == null) {
            this.originalAmount = BigDecimal.ZERO;
        if (this.principalPaid == null) {
            this.principalPaid = BigDecimal.ZERO;
        }
        if (this.profitPaid == null) {
            this.profitPaid = BigDecimal.ZERO;
        }
        }
        if (this.principalAmount == null) {
            this.principalAmount = BigDecimal.ZERO;
        }
        if (this.profitAmount == null) {
            this.profitAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

