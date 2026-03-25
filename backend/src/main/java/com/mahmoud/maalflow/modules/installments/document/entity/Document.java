package com.mahmoud.maalflow.modules.installments.document.entity;

import com.mahmoud.maalflow.modules.installments.document.enums.EntityType;
import com.mahmoud.maalflow.modules.installments.document.enums.FileType;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Document entity for storing files attached to various entities.
 * Supports attaching files to customers, vendors, purchases, contracts, and payments.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "file_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private EntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "file", columnDefinition = "MEDIUMBLOB")
    private byte[] file;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    /**
     * Constructor for creating a document with basic info.
     */
    public Document(EntityType entityType, Long entityId, String fileUrl, FileType fileType, String description) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileType=" + fileType +
                ", description='" + description + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}

