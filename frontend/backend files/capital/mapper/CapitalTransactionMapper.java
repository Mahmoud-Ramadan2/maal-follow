package com.mahmoud.maalflow.modules.installments.capital.mapper;

import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionRequest;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionResponse;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for CapitalTransaction entity and DTOs.
 * Handles conversion for pooled capital transactions with audit trail.
 *
 * @author Mahmoud
 */
@Mapper(componentModel = "spring")
public interface CapitalTransactionMapper {

    /**
     * Convert entity to response DTO with all audit trail fields.
     */
    @Mapping(target = "capitalPoolId", source = "capitalPool.id")
    @Mapping(target = "createdByUsername", source = "createdBy.name")
    @Mapping(target = "partnerName", ignore = true)
    CapitalTransactionResponse toResponse(CapitalTransaction capitalTransaction);

    /**
     * Convert request DTO to entity.
     * Most fields will be set by the service layer.
     */
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "referenceType", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "capitalPool", ignore = true)
    @Mapping(target = "availableBefore", ignore = true)
    @Mapping(target = "availableAfter", ignore = true)
    @Mapping(target = "lockedBefore", ignore = true)
    @Mapping(target = "lockedAfter", ignore = true)
    @Mapping(target = "referenceId", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    CapitalTransaction toEntity(CapitalTransactionRequest request);
}
