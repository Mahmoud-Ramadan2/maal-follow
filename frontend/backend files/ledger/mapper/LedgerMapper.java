package com.mahmoud.maalflow.modules.installments.ledger.mapper;

import com.mahmoud.maalflow.modules.installments.ledger.dto.LedgerRequest;
import com.mahmoud.maalflow.modules.installments.ledger.dto.LedgerResponse;
import com.mahmoud.maalflow.modules.installments.ledger.dto.LedgerSummary;
import com.mahmoud.maalflow.modules.installments.ledger.entity.DailyLedger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for DailyLedger entity and DTOs.
 *
 * @author Mahmoud
 */
@Mapper(componentModel = "spring")
public interface LedgerMapper {

    /**
     * Convert LedgerRequest to DailyLedger entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "partner", ignore = true)
    DailyLedger toEntity(LedgerRequest request);

    /**
     * Convert DailyLedger entity to LedgerResponse.
     */
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "partnerName", source = "partner.name")
    @Mapping(target = "partnerId", source = "partner.id")
    LedgerResponse toResponse(DailyLedger entity);

    /**
     * Convert DailyLedger entity to LedgerSummary.
     */
    LedgerSummary toSummary(DailyLedger entity);

    /**
     * Update existing DailyLedger entity from LedgerRequest.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "partner", ignore = true)
    void updateEntityFromRequest(LedgerRequest request, @MappingTarget DailyLedger entity);
}

