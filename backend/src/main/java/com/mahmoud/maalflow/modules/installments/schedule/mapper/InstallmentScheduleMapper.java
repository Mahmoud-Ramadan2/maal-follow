
package com.mahmoud.maalflow.modules.installments.schedule.mapper;

import com.mahmoud.maalflow.modules.installments.schedule.dto.InstallmentScheduleRequest;
import com.mahmoud.maalflow.modules.installments.schedule.dto.InstallmentScheduleResponse;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
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


//    @Mapping(target = "updatedAt", source = "")
//    @Mapping(target = "profitPaid", source = "")
//    @Mapping(target = "principalPaid", source = "")
//    @Mapping(target = "payments", source = "")
//    @Mapping(target = "createdAt", source = "")
//    @Mapping(target = "contractExpenses", source = "contractExpenses")
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