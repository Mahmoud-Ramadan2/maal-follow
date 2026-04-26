package com.mahmoud.maalflow.modules.installments.partner.mapper;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerWithdrawalRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerWithdrawalResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerWithdrawal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartnerWithdrawalMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profitAmount", ignore = true)
    @Mapping(target = "principalAmount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "rejectedBy", ignore = true)
    @Mapping(target = "partner", ignore = true)
    PartnerWithdrawal toPartnerWithdrawal(PartnerWithdrawalRequest partnerWithdrawalRequest);

    @Mapping(target = "partnerName", source = "partnerWithdrawal.partner.name")
    @Mapping(target = "processedByName", source = "partnerWithdrawal.processedBy.name")
    @Mapping(target = "approvedByName", source = "partnerWithdrawal.approvedBy.name")
    @Mapping(target = "rejectedByName", source = "partnerWithdrawal.rejectedBy.name")
    PartnerWithdrawalResponse toPartnerWithdrawalResponse(PartnerWithdrawal partnerWithdrawal);
}