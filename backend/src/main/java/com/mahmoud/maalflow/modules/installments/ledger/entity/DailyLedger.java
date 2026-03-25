package com.mahmoud.maalflow.modules.installments.ledger.entity;

import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerType;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily Ledger entity for tracking income and expenses.
 * Provides a complete financial record of all transactions.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "daily_ledger", indexes = {
        @Index(name = "idx_ledger_date", columnList = "date"),
        @Index(name = "idx_ledger_type", columnList = "type"),
        @Index(name = "idx_ledger_source", columnList = "source"),
        @Index(name = "idx_ledger_user", columnList = "user_id"),
        @Index(name = "idx_ledger_partner", columnList = "partner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Idempotency key to prevent duplicate ledger entries.
     */
    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LedgerType type;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private LedgerSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private LedgerReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @Override
    public String toString() {
        return "DailyLedger{" +
                "id=" + id +
                ", type=" + type +
                ", amount=" + amount +
                ", source=" + source +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }
}

