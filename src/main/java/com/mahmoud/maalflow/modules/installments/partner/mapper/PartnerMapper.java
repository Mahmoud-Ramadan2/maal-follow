package com.mahmoud.maalflow.modules.installments.partner.mapper;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartnerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "totalInvestment", ignore = true)
    @Mapping(target = "totalWithdrawals", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    Partner toPartner(PartnerRequest partnerRequest);

    @Mapping(target = "createdByName", expression = "java(partner.getCreatedBy() != null ? partner.getCreatedBy().getName() : null)")
    PartnerResponse toPartnerResponse(Partner partner);
}