package com.mahmoud.maalflow.modules.installments.schedule.repo;

import com.mahmoud.maalflow.modules.installments.schedule.entity.CollectionRouteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for CollectionRouteItem entity.
 */
@Repository
public interface CollectionRouteItemRepository extends JpaRepository<CollectionRouteItem, Long> {

    /**
     * Find items by collection route ID.
     */
    List<CollectionRouteItem> findByCollectionRouteIdOrderBySequenceOrderAsc(Long routeId);

    /**
     * Find active items by collection route ID.
     */
    List<CollectionRouteItem> findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(Long routeId);

    /**
     * Find items by customer ID.
     */
    List<CollectionRouteItem> findByCustomerId(Long customerId);

    /**
     * Check if customer exists in a specific route.
     */
    boolean existsByCollectionRouteIdAndCustomerId(Long routeId, Long customerId);

    /**
     * Get maximum sequence order for a route.
     */
    @Query("SELECT MAX(cri.sequenceOrder) FROM CollectionRouteItem cri WHERE cri.collectionRoute.id = :routeId")
    Integer findMaxSequenceOrderByRouteId(@Param("routeId") Long routeId);
}

