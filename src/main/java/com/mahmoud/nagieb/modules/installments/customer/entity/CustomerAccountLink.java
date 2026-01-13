package com.mahmoud.nagieb.modules.installments.customer.entity;

import com.mahmoud.nagieb.modules.installments.customer.enums.CustomerRelationshipType;
import com.mahmoud.nagieb.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


/**
 * Entity representing links between customer accounts.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "customer_account_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAccountLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 50)
    private CustomerRelationshipType relationshipType;

    @Column(name = "relationship_description", length = 255)
    private String relationshipDescription;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_customer_id", nullable = false)
    private Customer linkedCustomer;

    @Override
    public String toString() {
        return "CustomerAccountLink{" +
                "id=" + id +
                ", relationshipType=" + relationshipType +
                ", relationshipDescription='" + relationshipDescription + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}

