package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCustomerAcquisition;
import com.mahmoud.maalflow.modules.installments.partner.enums.CustomerAcquisitionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PartnerCustomerAcquisition entity.
 */
@Repository
public interface PartnerCustomerAcquisitionRepository extends JpaRepository<PartnerCustomerAcquisition, Long> {

    List<PartnerCustomerAcquisition> findByPartnerIdAndStatus(Long partnerId, CustomerAcquisitionStatus status);

    Optional<PartnerCustomerAcquisition> findByCustomerIdAndStatus(Long customerId, CustomerAcquisitionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pca FROM PartnerCustomerAcquisition pca WHERE pca.customer.id = :customerId AND pca.status = :status")
    Optional<PartnerCustomerAcquisition> findByCustomerIdAndStatusForUpdate(Long customerId, CustomerAcquisitionStatus status);

    Optional<PartnerCustomerAcquisition> findByPartnerIdAndCustomerIdAndStatus(Long partnerId, Long customerId, CustomerAcquisitionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pca FROM PartnerCustomerAcquisition pca WHERE pca.partner.id = :partnerId AND pca.customer.id = :customerId AND pca.status = :status")
    Optional<PartnerCustomerAcquisition> findByPartnerIdAndCustomerIdAndStatusForUpdate(Long partnerId, Long customerId, CustomerAcquisitionStatus status);

    List<PartnerCustomerAcquisition> findByPartnerId(Long partnerId);

    List<PartnerCustomerAcquisition> findByCustomerId(Long customerId);

    Optional<PartnerCustomerAcquisition> findByPartnerIdAndCustomerId(Long partnerId, Long customerId);

    @Query("SELECT COUNT(pca) FROM PartnerCustomerAcquisition pca WHERE pca.partner.id = :partnerId AND pca.status = :status")
    int countByPartnerIdAndStatus(Long partnerId, CustomerAcquisitionStatus status);

    @Query("SELECT SUM(pca.totalCommissionEarned) FROM PartnerCustomerAcquisition pca WHERE pca.partner.id = :partnerId AND pca.status = :status")
    BigDecimal sumCommissionByPartnerIdAndStatus(Long partnerId, CustomerAcquisitionStatus status);
}
