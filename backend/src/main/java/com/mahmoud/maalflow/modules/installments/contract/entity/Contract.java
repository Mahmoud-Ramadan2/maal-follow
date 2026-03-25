package com.mahmoud.maalflow.modules.installments.contract.entity;

import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * InstallmentContract entity representing installment sales contracts.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "installment_contract")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "contract_number", length = 50)
    private String contractNumber;

    @NotNull
    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @NotNull
    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice = BigDecimal.ZERO;

    @Column(name = "additional_costs", precision = 12, scale = 2)
    private BigDecimal additionalCosts = BigDecimal.ZERO;

    @Column(name = "cash_discount_rate", precision = 5, scale = 2)
    private BigDecimal cashDiscountRate = BigDecimal.ZERO;

    @Column(name = "early_payment_discount_rate", precision = 5, scale = 2)
    private BigDecimal earlyPaymentDiscountRate = BigDecimal.ZERO;

    @NotNull
    @Column(name = "profit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal profitAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "down_payment", nullable = false, precision = 12, scale = 2)
    private BigDecimal downPayment;

    @NotNull
    @Column(name = "remaining_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal remainingAmount;

    @NotNull
    @Column(name = "months", nullable = false)
    private Integer months;

    @NotNull
    @Column(name = "monthly_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyAmount;

    @NotNull
    @Column(name = "agreed_payment_day", nullable = false)
    private Integer agreedPaymentDay = 1;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status = ContractStatus.ACTIVE;

    @Column(name = "total_expenses", precision = 12, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    @Column(name = "net_profit", precision = 12, scale = 2)
    private BigDecimal netProfit = BigDecimal.ZERO;

    @Column(name = "capital_allocated", precision = 15, scale = 2)
    private BigDecimal capitalAllocated = BigDecimal.ZERO;

    @Column(name = "capital_returned", precision = 15, scale = 2)
    private BigDecimal capitalReturned = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;



    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InstallmentSchedule> installmentSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ContractExpense> expenses = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_purchase_id", nullable = false)
    private Purchase purchase;


    @PrePersist
    protected void onCreate() {
        if (this.contractNumber == null) {
            this.contractNumber = generateContractNumber();
        }
        if (this.status == null) {
            this.status = ContractStatus.ACTIVE;
        }
    }

    private String generateContractNumber() {

        String prefix = "CTR-";
        String uniquePart = java.util.UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase();
        return prefix + uniquePart;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", contractNumber='" + contractNumber +
                ", finalPrice=" + finalPrice +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}