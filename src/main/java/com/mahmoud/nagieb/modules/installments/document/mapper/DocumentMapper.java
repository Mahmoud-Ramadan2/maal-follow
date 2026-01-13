package com.mahmoud.nagieb.modules.installments.document.mapper;

import com.mahmoud.nagieb.modules.installments.document.dto.DocumentRequest;
import com.mahmoud.nagieb.modules.installments.document.dto.DocumentResponse;
import com.mahmoud.nagieb.modules.installments.document.dto.DocumentSummary;
import com.mahmoud.nagieb.modules.installments.document.entity.Document;
import com.mahmoud.nagieb.modules.installments.document.enums.EntityType;
import com.mahmoud.nagieb.modules.installments.document.enums.FileType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * MapStruct mapper for Document entity and DTOs.
 *
 * @author Mahmoud
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper {

    /**
     * Convert DocumentRequest to Document entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "file", ignore = true)
    Document toDocument(DocumentRequest request);

    /**
     * Convert Document entity to DocumentResponse.
     */
    @Mapping(target = "uploadedByName", source = "uploadedBy.name")
    DocumentResponse toDocumentResponse(Document document);

    /**
     * Convert Document entity to DocumentSummary.
     */
    DocumentSummary toDocumentSummary(Document document);

    /**
     * Update existing Document entity from DocumentRequest.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "file", ignore = true)
    void updateDocumentFromRequest(DocumentRequest request, @MappingTarget Document document);

}