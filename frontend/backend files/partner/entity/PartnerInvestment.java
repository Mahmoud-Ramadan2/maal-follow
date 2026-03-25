package com.mahmoud.maalflow.modules.installments.partner.entity;

import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentType;
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
 * Entity representing individual partner investments.
 */
@Entity
@Table(name = "partner_investment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerInvestment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", nullable = false, length = 20)
    private InvestmentType investmentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvestmentStatus status = InvestmentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "invested_at", nullable = false)
    private LocalDateTime investedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Override
    public String toString() {
        return "PartnerInvestment{" +
                "id=" + id +
                ", amount=" + amount +
                ", investmentType=" + investmentType +
                ", status=" + status +
                ", investedAt=" + investedAt +
                '}';
    }
}

