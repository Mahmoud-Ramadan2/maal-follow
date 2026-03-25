package com.mahmoud.maalflow.modules.associations.repo;

import com.mahmoud.maalflow.modules.associations.entity.AssociationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssociationMemberRepository extends JpaRepository<AssociationMember, Long> {
    List<AssociationMember> findByAssociationIdOrderByTurnOrderAsc(Long associationId);
    boolean existsByAssociationIdAndTurnOrder(Long associationId, Integer turnOrder);
    long countByAssociationId(Long associationId);
}

