package com.mahmoud.maalflow.modules.installments.partner.mapper;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerMonthlyProfitRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerMonthlyProfitResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartnerMonthlyProfitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "paidBy", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @Mapping(target = "profitDistribution", ignore = true)
    PartnerMonthlyProfit toPartnerMonthlyProfit(PartnerMonthlyProfitRequest partnerMonthlyProfitRequest);

    @Mapping(target = "partnerName", source = "partnerMonthlyProfit.partner.name")
    @Mapping(target = "profitDistributionMonth", source = "partnerMonthlyProfit.profitDistribution.monthYear")
    @Mapping(target = "paidByName", source = "partnerMonthlyProfit.paidBy.name")
    PartnerMonthlyProfitResponse toPartnerMonthlyProfitResponse(PartnerMonthlyProfit partnerMonthlyProfit);
}