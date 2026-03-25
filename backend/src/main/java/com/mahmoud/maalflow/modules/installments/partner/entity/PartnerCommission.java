package com.mahmoud.maalflow.modules.installments.partner.entity;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionType;
import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing partner commissions from customer acquisitions and sales.
 * Handles commission tracking for partners who bring customers or facilitate sales.
 */
@Entity
@Table(name = "partner_commission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerCommission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @NotNull
    @Column(name = "commission_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", nullable = false, length = 20)
    private CommissionType commissionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommissionStatus status = CommissionStatus.PENDING;

    @Column(name = "base_amount", precision = 12, scale = 2)
    private BigDecimal baseAmount; // The amount commission is calculated on

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
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Override
    public String toString() {
        return "PartnerCommission{" +
                "id=" + id +
                ", commissionAmount=" + commissionAmount +
                ", commissionPercentage=" + commissionPercentage +
                ", commissionType=" + commissionType +
                ", status=" + status +
                '}';
    }
}
