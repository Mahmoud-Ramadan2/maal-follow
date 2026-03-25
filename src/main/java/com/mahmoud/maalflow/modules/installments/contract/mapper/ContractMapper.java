package com.mahmoud.maalflow.modules.installments.contract.mapper;

import com.mahmoud.maalflow.modules.installments.contract.dto.ContractRequest;
import com.mahmoud.maalflow.modules.installments.contract.dto.ContractResponse;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Mahmoud
 */
@Mapper(componentModel = "spring")
public interface ContractMapper {


    @Mapping(target = "vendorName", source = "contract.purchase.vendor.name")
    @Mapping(target = "productName", source = "contract.purchase.productName")
    @Mapping(target = "customerName", source = "contract.customer.name")
    @Mapping(target = "partnerName", source = "contract.partner.name")
    @Mapping(target = "responsibleUserName", source = "contract.responsibleUser.name")
    ContractResponse toContractResponse(Contract contract);


    @Mapping(target = "cashDiscountRate", ignore = true)
    @Mapping(target = "remainingAmount", ignore = true)
    @Mapping(target = "profitAmount", ignore = true)
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "monthlyAmount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "purchase", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "responsibleUser", ignore = true)
    @Mapping(target = "installmentSchedules", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "totalExpenses", ignore = true)
    @Mapping(target = "netProfit", ignore = true)
    @Mapping(target = "completionDate", ignore = true)
    Contract toContract(ContractRequest contractRequest);
}