package com.mahmoud.nagieb.modules.installments.contract.entity;

import com.mahmoud.nagieb.modules.installments.contract.enums.ExpenseType;
import com.mahmoud.nagieb.modules.installments.contract.enums.PaidBy;
import com.mahmoud.nagieb.modules.installments.partner.entity.Partner;
import com.mahmoud.nagieb.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ContractExpense entity representing additional costs for contracts.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "contract_expense", indexes = {
        @Index(name = "idx_expense_contract", columnList = "installment_contract_id"),
        @Index(name = "idx_expense_type", columnList = "expense_type"),
        @Index(name = "idx_expense_date", columnList = "expense_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false)
    private ExpenseType expenseType;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 255)
    private String description;

    @NotNull
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "paid_by")
    private PaidBy paidBy = PaidBy.OWNER;



    @Column(name = "receipt_number", length = 100)
    private String receiptNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "ContractExpense{" +
                "id=" + id +
                ", expenseType=" + expenseType +
                ", amount=" + amount +
                ", expenseDate=" + expenseDate +
                '}';
    }
}

