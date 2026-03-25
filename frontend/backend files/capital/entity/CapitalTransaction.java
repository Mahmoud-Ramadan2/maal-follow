package com.mahmoud.maalflow.modules.installments.capital.entity;

import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing capital transaction with complete before/after audit trail.
 * Records all capital movements (investments, allocations, returns) for the shared pool.
 *
 * Audit Trail Features:
 * - Tracks available_amount before and after transaction
 * - Tracks locked_amount before and after transaction
 * - Enables verification of every balance change
 * - Immutable record for compliance
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "capital_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapitalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "capital_pool_id", nullable = false)
    private CapitalPool capitalPool;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private CapitalTransactionType transactionType;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Before/after balance tracking for complete audit trail
    @NotNull
    @Column(name = "available_before", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableBefore;

    @NotNull
    @Column(name = "available_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableAfter;

    @NotNull
    @Column(name = "locked_before", nullable = false, precision = 15, scale = 2)
    private BigDecimal lockedBefore;

    @NotNull
    @Column(name = "locked_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal lockedAfter;

    // Reference to related entity (optional)
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}

