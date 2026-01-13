package com.mahmoud.nagieb.modules.installments.document.dto;

import com.mahmoud.nagieb.modules.installments.document.enums.EntityType;
import com.mahmoud.nagieb.modules.installments.document.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for document details.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private EntityType entityType;
    private Long entityId;
    private String fileUrl;
    private FileType fileType;
    private String description;
    private LocalDateTime uploadedAt;
    private String uploadedByName;
}

