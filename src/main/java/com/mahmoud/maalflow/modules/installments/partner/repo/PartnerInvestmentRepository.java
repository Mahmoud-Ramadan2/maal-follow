package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerInvestment;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

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
}
