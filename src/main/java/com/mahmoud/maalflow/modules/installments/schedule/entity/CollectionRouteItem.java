package com.mahmoud.maalflow.modules.installments.schedule.entity;

import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private CollectionRoute collectionRoute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_route_item_customer"))
    private Customer customer;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder = 1;

    @Column(name = "estimated_collection_time")
    private LocalTime estimatedCollectionTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

