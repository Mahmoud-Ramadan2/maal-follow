package com.mahmoud.maalflow.modules.installments.partner.entity;

import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing partner's monthly profit calculation and payment.
 */
@Entity
@Table(name = "partner_monthly_profit",
       uniqueConstraints = @UniqueConstraint(columnNames = {"partner_id", "profit_distribution_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerMonthlyProfit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "investment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal investmentAmount;

    @NotNull
    @Column(name = "share_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal sharePercentage;

    @NotNull
    @Column(name = "calculated_profit", nullable = false, precision = 12, scale = 2)
    private BigDecimal calculatedProfit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProfitStatus status = ProfitStatus.CALCULATED;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private User paidBy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profit_distribution_id", nullable = false)
    private MonthlyProfitDistribution profitDistribution;


    @Override
    public String toString() {
        return "PartnerMonthlyProfit{" +
                "id=" + id +
                ", investmentAmount=" + investmentAmount +
                ", sharePercentage=" + sharePercentage +
                ", calculatedProfit=" + calculatedProfit +
                ", status=" + status +
                '}';
    }
}

