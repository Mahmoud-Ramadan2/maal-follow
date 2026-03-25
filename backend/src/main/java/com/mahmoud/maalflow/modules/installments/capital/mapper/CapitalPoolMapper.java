package com.mahmoud.maalflow.modules.installments.capital.mapper;

import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolRequest;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolResponse;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mapper for CapitalPool entity and DTOs.
 * Handles conversion and calculated fields for pooled capital model.
 *
 * @author Mahmoud
 */
@Mapper(componentModel = "spring")
public interface CapitalPoolMapper {


    /**
     * Convert entity to response DTO with calculated fields.
     */
    @Mapping(target = "ownerSharePercentage", expression = "java(calculateOwnerSharePercentage(capitalPool))")
    @Mapping(target = "partnerSharePercentage", expression = "java(calculatePartnersSharePercentage(capitalPool))")
    @Mapping(target = "utilizationPercentage", expression = "java(calculateUtilizationPercentage(capitalPool))")
    CapitalPoolResponse toResponse(CapitalPool capitalPool);

    /**
     * Convert request DTO to entity.
     */
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "returnedAmount", ignore = true)
    @Mapping(target = "lockedAmount",ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "availableAmount", ignore = true)
    CapitalPool toEntity(CapitalPoolRequest request);




    /**
     * Calculate owner share percentage.
     */
    default BigDecimal calculateOwnerSharePercentage(CapitalPool capitalPool) {
        if (capitalPool.getTotalAmount() == null || capitalPool.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return capitalPool.getOwnerContribution()
                .divide(capitalPool.getTotalAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate partner share percentage.
     */
    default BigDecimal calculatePartnersSharePercentage(CapitalPool capitalPool) {
        if (capitalPool.getTotalAmount() == null || capitalPool.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return capitalPool.getPartnerContributions()
                .divide(capitalPool.getTotalAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate capital utilization percentage (locked / total).
     */
    default BigDecimal calculateUtilizationPercentage(CapitalPool capitalPool) {
        if (capitalPool.getTotalAmount() == null || capitalPool.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal locked = capitalPool.getLockedAmount() != null ? capitalPool.getLockedAmount() : BigDecimal.ZERO;
        return locked
                .divide(capitalPool.getTotalAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
