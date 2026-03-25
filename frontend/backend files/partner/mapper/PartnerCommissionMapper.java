package com.mahmoud.maalflow.modules.installments.partner.mapper;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCommissionRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCommissionResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCommission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartnerCommissionMapper {

//    @Mapping(target = "status", source = "")
//    @Mapping(target = "calculatedAt", source = "")
//    @Mapping(target = "customer", source = "")
//    @Mapping(target = "commissionPercentage", source = "")
//    @Mapping(target = "commissionAmount", source = "")
//    @Mapping(target = "baseAmount", source = "")
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @Mapping(target = "contract", ignore = true)
    PartnerCommission toPartnerCommission(PartnerCommissionRequest partnerCommissionRequest);

    @Mapping(target = "purchaseProductName", ignore = true)
    @Mapping(target = "partnerName", source = "partnerCommission.partner.name")
    @Mapping(target = "contractCustomerName", source = "partnerCommission.contract.customer.name")
    PartnerCommissionResponse toPartnerCommissionResponse(PartnerCommission partnerCommission);
}