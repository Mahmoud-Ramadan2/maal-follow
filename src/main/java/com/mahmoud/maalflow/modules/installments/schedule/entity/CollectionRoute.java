package com.mahmoud.maalflow.modules.installments.schedule.entity;

import com.mahmoud.maalflow.modules.installments.schedule.enums.RouteType;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a collection route for payment collection optimization.
 */
@Entity
@Table(name = "collection_route")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", nullable = false)
    private RouteType routeType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_route_created_by"))
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "collectionRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionRouteItem> routeItems;
}

