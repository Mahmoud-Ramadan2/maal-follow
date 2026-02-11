package com.mahmoud.nagieb.modules.installments.contract.entity;

import com.mahmoud.nagieb.modules.installments.contract.enums.DeductionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing deductions from profit (management fees, zakat, etc.)
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "deduction", indexes = {
        @Index(name = "idx_deduction_contract", columnList = "contract_id"),
        @Index(name = "idx_deduction_schedule", columnList = "installment_schedule_id"),
        @Index(name = "idx_deduction_type", columnList = "deduction_type"),
        @Index(name = "idx_deduction_month", columnList = "month"),
        @Index(name = "idx_deduction_date", columnList = "deduction_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_schedule_id")
    private InstallmentSchedule installmentSchedule;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "deduction_type", nullable = false)
    private DeductionType deductionType;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "deduction_date", nullable = false)
    private LocalDate deductionDate;

    @NotNull
    @Column(name = "month", nullable = false, length = 7)
    private String month; // Format: YYYY-MM

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "Deduction{" +
                "id=" + id +
                ", deductionType=" + deductionType +
                ", amount=" + amount +
                ", month='" + month + '\'' +
                '}';
    }
}

