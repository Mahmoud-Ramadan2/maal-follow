package com.mahmoud.maalflow.modules.installments.document.dto;

import com.mahmoud.maalflow.modules.installments.document.enums.EntityType;
import com.mahmoud.maalflow.modules.installments.document.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Summary DTO for document listing.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSummary {

    private EntityType entityType;
    private Long entityId;
    private String fileUrl;
    private FileType fileType;
    private String description;
    private LocalDateTime uploadedAt;
}

