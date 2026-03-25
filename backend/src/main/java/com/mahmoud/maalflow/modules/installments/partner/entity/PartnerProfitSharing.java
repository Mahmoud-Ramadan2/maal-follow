package com.mahmoud.maalflow.modules.installments.partner.entity;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import com.mahmoud.maalflow.modules.installments.payment.entity.Payment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing partner profit sharing from contracts and payments.
 */
@Entity
@Table(name = "partner_profit_sharing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerProfitSharing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "share_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal sharePercentage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommissionStatus status = CommissionStatus.PENDING;

    @CreationTimestamp
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;


    @Override
    public String toString() {
        return "PartnerProfitSharing{" +
                "id=" + id +
                ", amount=" + amount +
                ", sharePercentage=" + sharePercentage +
                ", status=" + status +
                ", calculatedAt=" + calculatedAt +
                '}';
    }
}

