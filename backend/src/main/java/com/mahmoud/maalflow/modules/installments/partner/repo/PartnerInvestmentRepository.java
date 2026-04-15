package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerInvestment;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PartnerInvestment entity.
 */
@Repository
public interface PartnerInvestmentRepository extends JpaRepository<PartnerInvestment, Long> {

    List<PartnerInvestment> findByPartnerId(Long partnerId);

    List<PartnerInvestment> findByPartnerIdAndStatus(Long partnerId, InvestmentStatus status);

    @Query("SELECT SUM(pi.amount) FROM PartnerInvestment pi WHERE pi.partner.id = :partnerId AND pi.status = :status")
    BigDecimal sumByPartnerIdAndStatus(Long partnerId, InvestmentStatus status);


    boolean existsByPartnerId(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // Wait 3 seconds max
    @Query("SELECT pi FROM PartnerInvestment pi WHERE pi.id = :id")
    Optional<PartnerInvestment> findByIdForUpdate(@Param("id") Long id);

}
