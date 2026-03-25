package com.mahmoud.maalflow.modules.associations.entity;

import com.mahmoud.maalflow.modules.associations.enums.AssociationStatus;
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
 * Association entity - Rotating Savings (ROSCA / جمعية).
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/associations/entity/
 */
@Entity
@Table(name = "association")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Association {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull @Column(name = "monthly_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyAmount;

    @NotNull @Column(name = "total_members", nullable = false)
    private Integer totalMembers;

    @NotNull @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @NotNull @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AssociationStatus status = AssociationStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "association", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssociationMember> members = new ArrayList<>();

    public BigDecimal getTotalPoolAmount() {
        return monthlyAmount.multiply(BigDecimal.valueOf(totalMembers));
    }
}

