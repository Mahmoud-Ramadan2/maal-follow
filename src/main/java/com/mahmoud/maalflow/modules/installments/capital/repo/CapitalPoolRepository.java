package com.mahmoud.maalflow.modules.installments.capital.repo;

import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
