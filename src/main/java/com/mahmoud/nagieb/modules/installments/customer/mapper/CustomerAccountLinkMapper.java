package com.mahmoud.nagieb.modules.installments.customer.mapper;

import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerAccountLinkResponse;
import com.mahmoud.nagieb.modules.installments.customer.entity.CustomerAccountLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerAccountLinkMapper {

    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "linkedCustomer.name", target = "linkedCustomerName")
    @Mapping(source = "createdBy.name", target = "createdBy")
    CustomerAccountLinkResponse toCustomerAccountLinkResponse(CustomerAccountLink customerAccountLink);
}