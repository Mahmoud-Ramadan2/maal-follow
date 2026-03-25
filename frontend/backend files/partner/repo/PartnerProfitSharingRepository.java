package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerProfitSharing;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for PartnerProfitSharing entity.
 */
@Repository
public interface PartnerProfitSharingRepository extends JpaRepository<PartnerProfitSharing, Long> {

    List<PartnerProfitSharing> findByPartnerId(Long partnerId);

    List<PartnerProfitSharing> findByContractId(Long contractId);

    List<PartnerProfitSharing> findByPartnerIdAndStatus(Long partnerId, CommissionStatus status);

    @Query("SELECT SUM(pps.amount) FROM PartnerProfitSharing pps WHERE pps.partner.id = :partnerId AND pps.status = :status")
    BigDecimal sumByPartnerIdAndStatus(Long partnerId, CommissionStatus status);
}

