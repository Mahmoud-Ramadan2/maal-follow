
package com.mahmoud.maalflow.modules.installments.contract.mapper;

import com.mahmoud.maalflow.modules.installments.contract.dto.InstallmentScheduleRequest;
import com.mahmoud.maalflow.modules.installments.contract.dto.InstallmentScheduleResponse;
import com.mahmoud.maalflow.modules.installments.contract.entity.InstallmentSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Mahmoud
 */
@Mapper(componentModel = "spring")
public interface InstallmentScheduleMapper {

    @Mapping(target = "contractNumber", source = "contract.contractNumber")
    @Mapping(target = "customerName", source = "contract.customer.name")
    @Mapping(target = "customerPhone", source = "contract.customer.phone")
    @Mapping(target = "collectorName", source = "collector.name")
    InstallmentScheduleResponse toResponse(InstallmentSchedule schedule);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "collector", ignore = true)
    @Mapping(target = "originalAmount", ignore = true)
    @Mapping(target = "principalAmount", ignore = true)
    @Mapping(target = "profitAmount", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "paidDate", ignore = true)
    InstallmentSchedule toEntity(InstallmentScheduleRequest request);
}