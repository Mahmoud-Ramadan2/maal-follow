package com.mahmoud.maalflow.modules.installments.profit.entity;

import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing monthly profit distribution calculations.
 * Manages total profit, deductions (management fees, zakat), and distribution to owner and partners.
 */
@Entity
@Table(name = "monthly_profit_distribution",
       uniqueConstraints = @UniqueConstraint(columnNames = "month_year"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyProfitDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = "\\d{4}-\\d{2}")
    @Column(name = "month_year", nullable = false, length = 7, unique = true)
    private String monthYear;

    @NotNull
    @Column(name = "total_profit", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @Column(name = "management_fee_percentage", precision = 5, scale = 2)
    private BigDecimal managementFeePercentage = BigDecimal.ZERO;

    @Column(name = "zakat_percentage", precision = 5, scale = 2)
    private BigDecimal zakatPercentage = BigDecimal.ZERO;

    @Column(name = "management_fee_amount", precision = 12, scale = 2)
    private BigDecimal managementFeeAmount = BigDecimal.ZERO;

    @Column(name = "zakat_amount", precision = 12, scale = 2)
    private BigDecimal zakatAmount = BigDecimal.ZERO;

    @Column(name = "contract_expenses_amount", precision = 12, scale = 2)
    private BigDecimal contractExpensesAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "distributable_profit", nullable = false, precision = 15, scale = 2)
    private BigDecimal distributableProfit = BigDecimal.ZERO;

    @Column(name = "owner_profit", precision = 12, scale = 2)
    private BigDecimal ownerProfit = BigDecimal.ZERO;

    @Column(name = "partners_total_profit", precision = 12, scale = 2)
    private BigDecimal partnersTotalProfit = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProfitDistributionStatus status = ProfitDistributionStatus.PENDING;

    @Column(name = "calculation_notes", columnDefinition = "TEXT")
    private String calculationNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculated_by")
    private User calculatedBy;

    @Override
    public String toString() {
        return "MonthlyProfitDistribution{" +
                "id=" + id +
                ", monthYear='" + monthYear + '\'' +
                ", totalProfit=" + totalProfit +
                ", distributableProfit=" + distributableProfit +
                ", status=" + status +
                '}';
    }
}

