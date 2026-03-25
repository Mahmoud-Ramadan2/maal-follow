package com.mahmoud.maalflow.modules.associations.repo;

import com.mahmoud.maalflow.modules.associations.entity.Association;
import com.mahmoud.maalflow.modules.associations.enums.AssociationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/associations/repo/
 */
@Repository
public interface AssociationRepository extends JpaRepository<Association, Long> {
    Page<Association> findByStatus(AssociationStatus status, Pageable pageable);
    List<Association> findByStatusOrderByStartDateDesc(AssociationStatus status);
    Page<Association> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

