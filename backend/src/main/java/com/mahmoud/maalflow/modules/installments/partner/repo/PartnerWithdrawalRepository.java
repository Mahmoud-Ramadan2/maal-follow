package com.mahmoud.maalflow.modules.installments.partner.repo;

import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerWithdrawal;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.enums.WithdrawalStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PartnerWithdrawal entity.
 * @author Mahmoud
 */
@Repository
public interface PartnerWithdrawalRepository extends JpaRepository<PartnerWithdrawal, Long> {

    List<PartnerWithdrawal> findByPartnerId(Long partnerId);

    List<PartnerWithdrawal> findByStatus(WithdrawalStatus status);

    List<PartnerWithdrawal> findByPartnerIdAndStatus(Long partnerId, WithdrawalStatus status);

    @Query("SELECT SUM(pw.amount) FROM PartnerWithdrawal pw WHERE pw.partner.id = :partnerId AND pw.status = :status")
    BigDecimal sumByPartnerIdAndStatus(Long partnerId, WithdrawalStatus status);

    List<PartnerWithdrawal> findByPartnerAndRequestedAtBeforeOrderByRequestedAtAsc(Partner partner, LocalDateTime before);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // Wait 3 seconds max
    @Query("SELECT pw FROM PartnerWithdrawal pw WHERE pw.id = :id")
    Optional<PartnerWithdrawal> findWithdrawalByIdForUpdate(Long id);
}

