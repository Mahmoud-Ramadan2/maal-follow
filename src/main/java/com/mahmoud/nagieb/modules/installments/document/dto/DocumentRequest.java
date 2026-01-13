package com.mahmoud.nagieb.modules.installments.document.dto;

import com.mahmoud.nagieb.modules.installments.document.enums.EntityType;
import com.mahmoud.nagieb.modules.installments.document.enums.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating documents.
 *
 * @author Mahmoud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequest {

    @NotNull(message = "{validation.document.entityType.required}")
    private EntityType entityType;

    @NotNull(message = "{validation.document.entityId.required}")
    private Long entityId;

    @NotBlank(message = "{validation.document.fileUrl.required}")
    @Size(max = 500, message = "{validation.document.fileUrl.size}")
    private String fileUrl;

    @NotNull(message = "{validation.document.fileType.required}")
    private FileType fileType;

    @Size(max = 1000, message = "{validation.document.description.size}")
    private String description;
}

