package com.mahmoud.maalflow.modules.installments.document.dto;

import com.mahmoud.maalflow.modules.installments.document.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for uploading documents with file content.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadRequest {

    @NotNull(message = "{validation.document.entityType.required}")
    private EntityType entityType;

    @NotNull(message = "{validation.document.entityId.required}")
    private Long entityId;

    @NotNull(message = "{validation.document.file.required}")
    private MultipartFile file;

    @Size(max = 1000, message = "{validation.document.description.size}")
    private String description;
}