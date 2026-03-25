package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.CustomerAcquisitionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for partner customer acquisition operations.
 */
@Data
public class PartnerCustomerAcquisitionResponse {

    private Long id;
    private Long partnerId;
    private String partnerName;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private CustomerAcquisitionStatus status;
    private BigDecimal commissionPercentage;
    private BigDecimal totalCommissionEarned;
    private String acquisitionNotes;
    private LocalDateTime acquiredAt;
    private LocalDateTime deactivatedAt;
}
