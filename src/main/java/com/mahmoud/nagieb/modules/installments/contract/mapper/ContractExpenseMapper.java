package com.mahmoud.nagieb.modules.installments.contract.mapper;

import com.mahmoud.nagieb.modules.installments.contract.dto.ContractExpenseRequest;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractExpenseResponse;
import com.mahmoud.nagieb.modules.installments.contract.entity.ContractExpense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractExpenseMapper {

    @Mapping(target = "contractNumber", source = "contract.contractNumber")
    @Mapping(target = "partnerName", source = "partner.name")
    @Mapping(target = "createdByName", source = "createdBy.name")
    ContractExpenseResponse toResponse(ContractExpense expense);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ContractExpense toEntity(ContractExpenseRequest request);
}

