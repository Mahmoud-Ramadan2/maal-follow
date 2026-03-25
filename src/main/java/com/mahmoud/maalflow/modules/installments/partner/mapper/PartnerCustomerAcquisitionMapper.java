package com.mahmoud.maalflow.modules.installments.partner.mapper;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCustomerAcquisitionRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCustomerAcquisitionResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCustomerAcquisition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for PartnerCustomerAcquisition entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface PartnerCustomerAcquisitionMapper {

    PartnerCustomerAcquisitionMapper INSTANCE = Mappers.getMapper(PartnerCustomerAcquisitionMapper.class);

    /**
     * Convert entity to response DTO.
     */
    @Mapping(target = "partnerId", source = "partner.id")
    @Mapping(target = "partnerName", source = "partner.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "customerPhone", source = "customer.phone")
    PartnerCustomerAcquisitionResponse toResponse(PartnerCustomerAcquisition acquisition);

    /**
     * Convert request DTO to entity (partial mapping).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCommissionEarned", ignore = true)
    @Mapping(target = "acquiredAt", ignore = true)
    @Mapping(target = "deactivatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    PartnerCustomerAcquisition toEntity(PartnerCustomerAcquisitionRequest request);
}
