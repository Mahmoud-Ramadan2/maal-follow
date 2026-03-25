package com.mahmoud.maalflow.modules.associations.repo;

import com.mahmoud.maalflow.modules.associations.entity.AssociationPayment;
import com.mahmoud.maalflow.modules.associations.enums.MemberPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssociationPaymentRepository extends JpaRepository<AssociationPayment, Long> {
    List<AssociationPayment> findByAssociationIdAndPaymentMonth(Long associationId, String month);
    List<AssociationPayment> findByMemberIdOrderByPaymentMonthDesc(Long memberId);
    List<AssociationPayment> findByAssociationIdAndStatus(Long associationId, MemberPaymentStatus status);
    boolean existsByMemberIdAndPaymentMonth(Long memberId, String paymentMonth);

    @Query("SELECT COUNT(p) FROM AssociationPayment p WHERE p.member.id = :memberId AND p.status = 'PAID'")
    int countPaidByMemberId(Long memberId);
}

