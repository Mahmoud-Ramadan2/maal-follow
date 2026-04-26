package com.mahmoud.maalflow.modules.installments.collection.repo;

import com.mahmoud.maalflow.modules.installments.collection.entity.CollectionRoute;
import com.mahmoud.maalflow.modules.installments.collection.enums.RouteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for CollectionRoute entity.
 */
@Repository
public interface CollectionRouteRepository extends JpaRepository<CollectionRoute, Long> {

    /**
     * Find active collection routes.
     */
    List<CollectionRoute> findByIsActiveTrue();

    /**
     * Find routes by type.
     */
    List<CollectionRoute> findByRouteType(RouteType routeType);

    /**
     * Find active routes by type.
     */
    List<CollectionRoute> findByRouteTypeAndIsActiveTrue(RouteType routeType);

    /**
     * Find routes created by a specific user.
     */
    List<CollectionRoute> findByCreatedById(Long userId);

    List<CollectionRoute> findByIsActiveTrueOrderByNameAsc();
}

