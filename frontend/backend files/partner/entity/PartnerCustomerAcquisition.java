package com.mahmoud.maalflow.modules.installments.partner.entity;

import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.partner.enums.CustomerAcquisitionStatus;
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
 * Entity representing customer acquisition by partners.
 * Tracks which partner brought which customer and associated commissions.
 */
@Entity
@Table(name = "partner_customer_acquisition",
       uniqueConstraints = @UniqueConstraint(columnNames = {"partner_id", "customer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCustomerAcquisition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomerAcquisitionStatus status = CustomerAcquisitionStatus.ACTIVE;

    @Column(name = "commission_percentage", precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(name = "total_commission_earned", precision = 12, scale = 2)
    private BigDecimal totalCommissionEarned = BigDecimal.ZERO;

    @Column(name = "acquisition_notes", columnDefinition = "TEXT")
    private String acquisitionNotes;

    @CreationTimestamp
    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Override
    public String toString() {
        return "PartnerCustomerAcquisition{" +
                "id=" + id +
                ", status=" + status +
                ", commissionPercentage=" + commissionPercentage +
                '}';
    }
}
