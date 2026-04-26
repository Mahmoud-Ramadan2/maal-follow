package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnershipType;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Partner entity.
 */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Partner p WHERE p.id = :id")
    Optional<Partner> findByIdForUpdate(Long id);

    Optional<Partner> findByIdAndStatus(Long id, PartnerStatus status);

    List<Partner> findByStatus(PartnerStatus status);

    Optional<Partner> findByPhone(String phone);

    List<Partner> findByPartnershipType(PartnershipType partnershipType);

    boolean existsByPhone(String phone);

    List<Partner> findByStatusAndProfitSharingActive(PartnerStatus status, Boolean profitSharingActive);

    List<Partner> findByProfitSharingActive(Boolean profitSharingActive);

    @Query("SELECT COALESCE(SUM(p.totalInvestment), 0) FROM Partner p")
    BigDecimal sumTotalInvestment();

    @Query("SELECT COALESCE(SUM(p.totalWithdrawals), 0) FROM Partner p")
    BigDecimal sumTotalWithdrawals();

    boolean existsByNationalId(String nationalId);

    @Query("SELECT COALESCE(SUM(p.effectiveInvestment), 0) FROM Partner p " +
            "WHERE p.status = 'ACTIVE' AND p.profitSharingActive = true")
    BigDecimal sumEffectiveInvestmentByActivePartners();

    @Query("""
SELECT p FROM Partner p
WHERE p.status = 'ACTIVE' AND p.profitSharingActive = true
""")
    List<Partner> findAllActivePartners();
}
