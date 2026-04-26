package com.mahmoud.maalflow.modules.installments.payment.entity;

import com.mahmoud.maalflow.modules.installments.payment.enums.DiscountType;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for tracking payment discount configurations and calculations.
 */
@Entity
@Table(name = "payment_discount_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDiscountConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "early_payment_days_threshold", nullable = false)
    private Integer earlyPaymentDaysThreshold = 5; // Days before due date

    @Column(name = "early_payment_discount_percentage", precision = 5, scale = 2)
    private BigDecimal earlyPaymentDiscountPercentage = BigDecimal.valueOf(0.0); // 0% default

    @Column(name = "final_installment_discount_percentage", precision = 5, scale = 2)
    private BigDecimal finalInstallmentDiscountPercentage = BigDecimal.valueOf(0.0); // 0% default

    @Column(name = "minimum_discount_amount", precision = 10, scale = 2)
    private BigDecimal minimumDiscountAmount = BigDecimal.valueOf(10.0);

    @Column(name = "maximum_discount_amount", precision = 10, scale = 2)
    private BigDecimal maximumDiscountAmount = BigDecimal.valueOf(1000.0);

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Override
    public String toString() {
        return "PaymentDiscountConfig{" +
                "id=" + id +
                ", discountType=" + discountType +
                ", earlyPaymentDaysThreshold=" + earlyPaymentDaysThreshold +
                ", earlyPaymentDiscountPercentage=" + earlyPaymentDiscountPercentage +
                '}';
    }
}
