package com.mahmoud.nagieb.modules.installments.customer.mapper;

import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerRequest;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerResponse;
import com.mahmoud.nagieb.modules.installments.customer.dto.CustomerSummary;
import com.mahmoud.nagieb.modules.installments.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Customer toCustomer(CustomerRequest customerRequest);

    CustomerResponse toCustomerResponse(Customer customer);

    CustomerSummary toCustomerSummary(Customer customer);




}
