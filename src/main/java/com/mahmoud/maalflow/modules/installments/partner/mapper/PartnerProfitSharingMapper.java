package com.mahmoud.maalflow.modules.installments.partner.mapper;

import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerProfitSharingRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerProfitSharingResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerProfitSharing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartnerProfitSharingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "calculatedAt", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "payment", ignore = true)
    PartnerProfitSharing toPartnerProfitSharing(PartnerProfitSharingRequest partnerProfitSharingRequest);

    @Mapping(target = "partnerName", source = "partnerProfitSharing.partner.name")
    @Mapping(target = "contractCustomerName", source = "partnerProfitSharing.contract.customer.name")
    @Mapping(target = "paymentDetails", expression = "java(getPaymentDetails(partnerProfitSharing))")
    PartnerProfitSharingResponse toPartnerProfitSharingResponse(PartnerProfitSharing partnerProfitSharing);

    default String getPaymentDetails(PartnerProfitSharing partnerProfitSharing) {
        if (partnerProfitSharing.getPayment() != null) {
            return "Payment ID: " + partnerProfitSharing.getPayment().getId() + 
                   ", Amount: " + partnerProfitSharing.getPayment().getAmount();
        }
        return null;
    }
}