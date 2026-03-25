package com.mahmoud.maalflow.modules.installments.partner.mapper;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerInvestment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartnerInvestmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "investedAt", ignore = true)
    @Mapping(target = "returnedAt", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @Mapping(target = "investmentType", ignore = true)
    PartnerInvestment toPartnerInvestment(PartnerInvestmentRequest partnerInvestmentRequest);

    @Mapping(target = "partnerName", source = "partnerInvestment.partner.name")
    PartnerInvestmentResponse toPartnerInvestmentResponse(PartnerInvestment partnerInvestment);
}