package com.mahmoud.maalflow.modules.installments.capital.repo;

import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repository for CapitalPool entity.
 */
@Repository
public interface CapitalPoolRepository extends JpaRepository<CapitalPool, Long> {

    /**
     * Get the current active capital pool (latest entry).
     */
    Optional<CapitalPool> findTopByOrderByCreatedAtDesc();

    /**
     * Get total capital amount from the latest capital pool.
     */
    @Query("SELECT cp.totalAmount FROM CapitalPool cp ORDER BY cp.createdAt DESC LIMIT 1")
    Optional<BigDecimal> getCurrentTotalCapital();

    /**
     * Get owner contribution from the latest capital pool.
     */
    @Query("SELECT cp.ownerContribution FROM CapitalPool cp ORDER BY cp.createdAt DESC LIMIT 1")
    Optional<BigDecimal> getCurrentOwnerContribution();

    /**
     * Get partner contributions from the latest capital pool.
     */
    @Query("SELECT cp.partnerContributions FROM CapitalPool cp ORDER BY cp.createdAt DESC LIMIT 1")
    Optional<BigDecimal> getCurrentPartnerContributions();
    
    /**
     * Find capital pool by ID with pessimistic write lock to prevent concurrent modifications.
     * Wait 3 seconds max
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // Wait 3 seconds max
    @Query("SELECT cp FROM CapitalPool cp WHERE cp.id = :id")
    Optional<CapitalPool> findByIdForUpdate(@Param("id") Long id);
}
