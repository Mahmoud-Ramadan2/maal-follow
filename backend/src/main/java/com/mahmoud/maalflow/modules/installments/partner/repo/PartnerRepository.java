package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Partner entity.
 */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Optional<Partner> findByIdAndStatus(Long id, PartnerStatus status);

    List<Partner> findByStatus(PartnerStatus status);

    Optional<Partner> findByPhone(String phone);

    List<Partner> findByPartnershipType(PartnershipType partnershipType);

    boolean existsByPhone(String phone);

    List<Partner> findByStatusAndProfitSharingActive(PartnerStatus status, Boolean profitSharingActive);
}

