package com.mahmoud.maalflow.modules.installments.partner.entity;

import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for configuring profit calculation parameters for the system.
 * Handles management fees, zakat percentages, and profit distribution settings.
 */
@Entity
@Table(name = "partner_profit_calculation_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerProfitCalculationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "management_fee_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal managementFeePercentage = BigDecimal.valueOf(5.00); // Default 5%

    @NotNull
    @Column(name = "zakat_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal zakatPercentage = BigDecimal.valueOf(2.50); // Default 2.5%

    @NotNull
    @Column(name = "profit_payment_day", nullable = false)
    private Integer profitPaymentDay = 10; // Default 10th of each month

    @NotNull
    @Column(name = "new_partner_delay_months", nullable = false)
    private Integer newPartnerDelayMonths = 2; // Default 2 months

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Override
    public String toString() {
        return "PartnerProfitCalculationConfig{" +
                "id=" + id +
                ", managementFeePercentage=" + managementFeePercentage +
                ", zakatPercentage=" + zakatPercentage +
                ", profitPaymentDay=" + profitPaymentDay +
                ", newPartnerDelayMonths=" + newPartnerDelayMonths +
                '}';
    }
}
