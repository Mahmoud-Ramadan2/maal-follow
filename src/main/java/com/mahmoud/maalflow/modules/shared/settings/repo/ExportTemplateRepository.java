package com.mahmoud.maalflow.modules.shared.settings.repo;

import com.mahmoud.maalflow.modules.shared.settings.entity.ExportTemplate;
import com.mahmoud.maalflow.modules.shared.settings.enums.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ExportTemplate entity.
 */
@Repository
public interface ExportTemplateRepository extends JpaRepository<ExportTemplate, Long> {

    /**
     * Find templates by type.
     */
    List<ExportTemplate> findByTemplateType(TemplateType templateType);

    /**
     * Find default template by type.
     */
    Optional<ExportTemplate> findByTemplateTypeAndIsDefaultTrue(TemplateType templateType);

    /**
     * Find templates created by a specific user.
     */
    List<ExportTemplate> findByCreatedById(Long userId);

    /**
     * Find all default templates.
     */
    List<ExportTemplate> findByIsDefaultTrue();
}

