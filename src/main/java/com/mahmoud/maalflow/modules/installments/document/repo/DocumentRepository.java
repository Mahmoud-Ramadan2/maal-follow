
package com.mahmoud.maalflow.modules.installments.document.repo;

import com.mahmoud.maalflow.modules.installments.document.entity.Document;
import com.mahmoud.maalflow.modules.installments.document.enums.EntityType;
import com.mahmoud.maalflow.modules.installments.document.enums.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Document entity with comprehensive query methods.
 *
 * @author Mahmoud
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Find all documents for a specific entity.
     */
    List<Document> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    /**
     * Find all documents for a specific entity with pagination.
     */
    Page<Document> findByEntityTypeAndEntityId(EntityType entityType, Long entityId, Pageable pageable);

    /**
     * Find all documents by entity type.
     */
    Page<Document> findByEntityType(EntityType entityType, Pageable pageable);

    /**
     * Find all documents by file type.
     */
    Page<Document> findByFileType(FileType fileType, Pageable pageable);

    /**
     * Find documents uploaded by a specific user.
     */
    Page<Document> findByUploadedById(Long userId, Pageable pageable);

    /**
     * Find documents uploaded within a date range.
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt BETWEEN :startDate AND :endDate")
    Page<Document> findByUploadedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Count documents for a specific entity.
     */
    long countByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    /**
     * Check if a document exists for a specific entity.
     */
    boolean existsByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    /**
     * Delete all documents for a specific entity.
     */
    void deleteByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    /**
     * Search documents by description.
     */
    @Query("SELECT d FROM Document d WHERE LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Document> searchByDescription(@Param("search") String search, Pageable pageable);

    /**
     * Find documents by entity type and file type.
     */
    Page<Document> findByEntityTypeAndFileType(EntityType entityType, FileType fileType, Pageable pageable);

    /**
     * Get all documents with pagination.
     */
    Page<Document> findAll(Pageable pageable);

    /**
     * Count documents by entity type.
     */
    long countByEntityType(EntityType entityType);

    /**
     * Count documents by file type.
     */
    long countByFileType(FileType fileType);
}

