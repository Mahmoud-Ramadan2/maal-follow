package com.mahmoud.maalflow.modules.installments.collection.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mahmoud.maalflow.modules.installments.collection.enums.CollectionItemStatus;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;

/**
 * Entity representing customers assigned to collection routes.
 */
@Entity
@Table(name = "collection_route_item", indexes = {
    @Index(name = "idx_route_sequence", columnList = "collection_route_id, sequence_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRouteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_route_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_route_item_route"))
    @JsonIgnoreProperties({"routeItems", "createdBy"})
    private CollectionRoute collectionRoute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_route_item_customer"))
    @JsonIgnoreProperties({"contracts", "accountLinks", "linkedBy", "collectionRouteItems", "createdBy"})
    private Customer customer;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder = 1;

    @Column(name = "estimated_collection_time")
    private LocalTime estimatedCollectionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_status", nullable = false)
    private CollectionItemStatus collectionStatus = CollectionItemStatus.PENDING;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "collected_amount", precision = 12, scale = 2)
    private BigDecimal collectedAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

