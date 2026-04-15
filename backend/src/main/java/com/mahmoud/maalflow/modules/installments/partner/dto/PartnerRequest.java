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

    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{validation.phone.pattern}")
    private String phone;

    @Size(max = 200, message = "{validation.address.size}")
    private String address;

//    @NotNull(message = "{validation.partnershipType.required}")
    private PartnershipType partnershipType;

//    @DecimalMin(value = "0.01", message = "{validation.sharePercentage.invalid}")
//    @DecimalMax(value = "100.00", message = "{validation.sharePercentage.invalid}")
//    @Digits(integer = 3, fraction = 2, message = "{validation.sharePercentage.format}")
//    private BigDecimal sharePercentage;

    private PartnerStatus status;

    @NotNull(message = "{validation.investmentStartDate.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate investmentStartDate;

    @Size(max = 7, message = "{validation.profitCalculationStartMonth.size}")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "{validation.profitCalculationStartMonth.pattern}")
    private String profitCalculationStartMonth;

    @NotNull(message = "{validation.partner.totalInvestment.required}")
    @DecimalMin(value = "100", message = "{validation.partner.totalInvestment.min}")
    @Digits(integer = 12, fraction = 2, message = "{validation.partner.totalInvestment.format}")
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
