package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.PartnershipType;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerRequest {

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 4, max = 200, message = "{validation.name.size}")
    private String name;

    @NotBlank(message = "{messages.partner.phone.required}")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{messages.partner.phone.pattern}")
    private String phone;

    @NotBlank(message ="{validation.nationalId.required}")
    @Size(min = 6, max = 14, message = "{validation.nationalId.size}")
    @Pattern(regexp = "^[0-9]+$", message = "{validation.nationalId.pattern}")
    private String nationalId;

    @Size(max = 200, message = "{validation.address.size}")
    private String address;

//    @NotNull(message = "{messages.partner.partnershipType.required}")
    private PartnershipType partnershipType;

//    @DecimalMin(value = "0.01", message = "{messages.partner.sharePercentage.invalid}")
//    @DecimalMax(value = "100.00", message = "{messages.partner.sharePercentage.invalid}")
//    @Digits(integer = 3, fraction = 2, message = "{messages.partner.sharePercentage.format}")
//    private BigDecimal sharePercentage;

    private PartnerStatus status;

    @NotNull(message = "{messages.partner.investmentStartDate.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate investmentStartDate;

    @Size(max = 7, message = "{messages.partner.profitCalculationStartMonth.size}")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "{messages.partner.profitCalculationStartMonth.pattern}")
    private String profitCalculationStartMonth;

    @NotNull(message = "{messages.partner.totalInvestment.required}")
    @DecimalMin(value = "100", message = "{messages.partner.totalInvestment.min}")
    @Digits(integer = 12, fraction = 2, message = "{messages.partner.totalInvestment.format}")
    private BigDecimal totalInvestment;


//    private BigDecimal totalWithdrawals;
//
//    private BigDecimal currentBalance;

    private Boolean profitSharingActive;

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;

    @NotNull(message = "{validation.createdBy.required}")
    private Long createdBy;


}

