package com.mahmoud.maalflow.modules.associations.entity;

import com.mahmoud.maalflow.modules.associations.enums.MemberPaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment record for an association member.
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/associations/entity/
 */
@Entity
@Table(name = "association_payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssociationPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private AssociationMember member;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_month", nullable = false, length = 7)
    private String paymentMonth; // YYYY-MM

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private MemberPaymentStatus status = MemberPaymentStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

