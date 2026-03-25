package com.mahmoud.maalflow.modules.debts.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment against a debt record.
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/debts/entity/
 */
@Entity
@Table(name = "debt_payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DebtPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    @NotNull @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod; // CASH, VODAFONE_CASH, BANK_TRANSFER, OTHER

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

