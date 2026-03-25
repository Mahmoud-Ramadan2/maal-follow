package com.mahmoud.maalflow.modules.installments.payment.mapper;

import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentDiscountConfigRequest;
import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentDiscountConfigResponse;
import com.mahmoud.maalflow.modules.installments.payment.entity.PaymentDiscountConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper for PaymentDiscountConfig entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface PaymentDiscountConfigMapper {

    /**
     * Convert entity to response DTO.
     */
    @Mapping(target = "createdByName", source = "createdBy.name")
    PaymentDiscountConfigResponse toResponse(PaymentDiscountConfig config);

    /**
     * Convert request DTO to entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    PaymentDiscountConfig toEntity(PaymentDiscountConfigRequest request);

    /**
     * Update existing entity from request DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromRequest(PaymentDiscountConfigRequest request, @MappingTarget PaymentDiscountConfig config);
}
