package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCommission;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for PartnerCommission entity.
 */
@Repository
public interface PartnerCommissionRepository extends JpaRepository<PartnerCommission, Long> {

    // Basic finder methods
    List<PartnerCommission> findByPartnerId(Long partnerId);

    Page<PartnerCommission> findByPartnerId(Long partnerId, Pageable pageable);

    List<PartnerCommission> findByContractId(Long contractId);

    List<PartnerCommission> findByPurchaseId(Long purchaseId);

    List<PartnerCommission> findByPartnerIdAndStatus(Long partnerId, CommissionStatus status);

    Page<PartnerCommission> findByStatus(CommissionStatus status, Pageable pageable);

    // Existence checks
    boolean existsByPartnerIdAndCustomerIdAndCommissionType(Long partnerId, Long customerId, CommissionType commissionType);

    boolean existsByPartnerIdAndContractIdAndCommissionType(Long partnerId, Long contractId, CommissionType commissionType);

    // Count methods
    long countByPartnerIdAndStatus(Long partnerId, CommissionStatus status);

    // Sum methods
    @Query("SELECT COALESCE(SUM(pc.commissionAmount), 0) FROM PartnerCommission pc WHERE pc.partner.id = :partnerId AND pc.status = :status")
    BigDecimal sumByPartnerIdAndStatus(@Param("partnerId") Long partnerId, @Param("status") CommissionStatus status);

    @Query("SELECT COALESCE(SUM(pc.commissionAmount), 0) FROM PartnerCommission pc WHERE pc.partner.id = :partnerId")
    BigDecimal sumByPartnerId(@Param("partnerId") Long partnerId);

    // Monthly commission reports
    @Query("SELECT pc FROM PartnerCommission pc WHERE pc.partner.id = :partnerId AND YEAR(pc.calculatedAt) = :year AND MONTH(pc.calculatedAt) = :month")
    List<PartnerCommission> findByPartnerIdAndMonth(@Param("partnerId") Long partnerId, @Param("year") int year, @Param("month") int month);

    // Commission type specific queries
    List<PartnerCommission> findByPartnerIdAndCommissionType(Long partnerId, CommissionType commissionType);

    @Query("SELECT COALESCE(SUM(pc.commissionAmount), 0) FROM PartnerCommission pc WHERE pc.partner.id = :partnerId AND pc.commissionType = :commissionType AND pc.status = :status")
    BigDecimal sumByPartnerIdAndCommissionTypeAndStatus(@Param("partnerId") Long partnerId, @Param("commissionType") CommissionType commissionType, @Param("status") CommissionStatus status);
}

