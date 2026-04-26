package com.mahmoud.maalflow.modules.installments.capital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing the shared capital pool.
 * Tracks total capital, available (unallocated), locked (in contracts), and returned amounts.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "capital_pool")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapitalPool {

    @Id
    // its just have one row with id = 1
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // using Pessimistic lock rather than Optimistic
//    @Version
//    @Column(name = "version", nullable = false)
//    private Long version;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "available_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableAmount = BigDecimal.ZERO;

    @Column(name = "locked_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal lockedAmount = BigDecimal.ZERO;

    @Column(name = "returned_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal returnedAmount = BigDecimal.ZERO;

    @Column(name = "owner_contribution", nullable = false, precision = 15, scale = 2)
    private BigDecimal ownerContribution = BigDecimal.ZERO;

    @Column(name = "partner_contributions", nullable = false, precision = 15, scale = 2)
    private BigDecimal partnerContributions = BigDecimal.ZERO;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
