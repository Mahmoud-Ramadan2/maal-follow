package com.mahmoud.maalflow.modules.associations.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Member of an association (جمعية).
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/associations/entity/
 */
@Entity
@Table(name = "association_member")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssociationMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;

    @Column(name = "member_name", nullable = false, length = 200)
    private String memberName;

    @Column(length = 20)
    private String phone;

    @Column(name = "turn_order", nullable = false)
    private Integer turnOrder;

    @Column(name = "has_received")
    private Boolean hasReceived = false;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AssociationPayment> payments = new ArrayList<>();
}

