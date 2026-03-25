package com.mahmoud.maalflow.modules.installments.customer.mapper;

import com.mahmoud.maalflow.modules.installments.customer.dto.CustomerRequest;
import com.mahmoud.maalflow.modules.installments.customer.dto.CustomerResponse;
import com.mahmoud.maalflow.modules.installments.customer.dto.CustomerSummary;
import com.mahmoud.maalflow.modules.installments.customer.dto.CustomerWithContarctsResponse;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface CustomerMapper {


    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "contracts", ignore = true)
    @Mapping(target = "linkedBy", ignore = true)
    @Mapping(target = "collectionRouteItems", ignore = true)
    @Mapping(target = "accountLinks", ignore = true)
    Customer toCustomer(CustomerRequest customerRequest);

    @Mapping(target = "createdBy", source = "customer.createdBy.name")
    CustomerResponse toCustomerResponse(Customer customer);

    CustomerSummary toCustomerSummary(Customer customer);

//    @Mapping(target = "customer", source = "customer")
@Mapping(target = "createdBy", source = "customer.createdBy.name")
CustomerWithContarctsResponse toCustomerWithContarctsResponse(Customer customer);

}
