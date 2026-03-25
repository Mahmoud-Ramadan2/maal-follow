package com.mahmoud.maalflow.modules.installments.partner.entity;

import com.mahmoud.maalflow.modules.installments.partner.enums.PartnershipType;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a business partner (investor, affiliate, distributor).
 */
@Entity
@Table(name = "partner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Partner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "partnership_type", nullable = false, length = 20)
    private PartnershipType partnershipType = PartnershipType.INVESTOR;

    @Column(name = "share_percentage", precision = 5, scale = 2)
    private BigDecimal sharePercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PartnerStatus status = PartnerStatus.ACTIVE;

    @Column(name = "investment_start_date")
    private LocalDate investmentStartDate;

    @Size(max = 7)
    @Column(name = "profit_calculation_start_month", length = 7)
    private String profitCalculationStartMonth;

    @ColumnDefault("0.00")
    @Column(name = "total_investment", precision = 15, scale = 2)
    private BigDecimal totalInvestment;

    @ColumnDefault("0.00")
    @Column(name = "total_withdrawals", precision = 15, scale = 2)
    private BigDecimal totalWithdrawals;

    @ColumnDefault("0.00")
    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "profit_sharing_active")
    private Boolean profitSharingActive = true;

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;


    @Override
    public String toString() {
        return "Partner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", partnershipType=" + partnershipType +
                ", status=" + status +
                ", totalInvestment=" + totalInvestment +
                ", currentBalance=" + currentBalance +
                '}';
    }
}

