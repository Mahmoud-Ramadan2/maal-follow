package com.mahmoud.maalflow.modules.debts.entity;

import com.mahmoud.maalflow.modules.debts.enums.DebtStatus;
import com.mahmoud.maalflow.modules.debts.enums.DebtType;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
 * Debt entity - tracks money owed to/from the user.
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/debts/entity/
 */
@Entity
@Table(name = "debt")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Debt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(name = "person_name", nullable = false, length = 200)
    private String personName;

    @Column(length = 20)
    private String phone;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = "debt_type", nullable = false)
    private DebtType debtType;

    @NotNull @Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalAmount;

    @NotNull @Column(name = "remaining_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal remainingAmount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DebtStatus status = DebtStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "debt", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DebtPayment> payments = new ArrayList<>();
}

