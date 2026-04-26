package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit;
import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for PartnerMonthlyProfit entity.
 */
@Repository
public interface PartnerMonthlyProfitRepository extends JpaRepository<PartnerMonthlyProfit, Long> {

    List<PartnerMonthlyProfit> findByPartnerId(Long partnerId);

    List<PartnerMonthlyProfit> findByProfitDistributionId(Long profitDistributionId);

    List<PartnerMonthlyProfit> findByPartnerIdAndStatus(Long partnerId, ProfitStatus status);

    @Query("SELECT SUM(pmp.calculatedProfit) FROM PartnerMonthlyProfit pmp WHERE pmp.partner.id = :partnerId AND pmp.status = :status")
    BigDecimal sumProfitByPartnerIdAndStatus(Long partnerId, ProfitStatus status);


    boolean existsByProfitDistributionIdAndStatusNot(Long id, ProfitStatus profitStatus);

    void deleteByProfitDistributionId(Long id);
}
