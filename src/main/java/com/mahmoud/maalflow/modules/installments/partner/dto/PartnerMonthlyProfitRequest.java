package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
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
public class PartnerMonthlyProfitRequest {

    @NotNull(message = "{validation.partnerId.required}")
    private Long partnerId;

    @NotNull(message = "{validation.profitDistributionId.required}")
    private Long profitDistributionId;

    @NotNull(message = "{validation.investmentAmount.required}")
    @DecimalMin(value = "0.01", message = "{validation.investmentAmount.min}")
    @Digits(integer = 12, fraction = 2, message = "{validation.investmentAmount.format}")
    private BigDecimal investmentAmount;

    @NotNull(message = "{validation.sharePercentage.required}")
    @DecimalMin(value = "0.00", message = "{validation.sharePercentage.invalid}")
    @DecimalMax(value = "10.00", message = "{validation.sharePercentage.invalid}")
    @Digits(integer = 5, fraction = 2, message = "{validation.sharePercentage.format}")
    private BigDecimal sharePercentage;

    @NotNull(message = "{validation.calculatedProfit.required}")
    @DecimalMin(value = "0.01", message = "{validation.calculatedProfit.min}")
    @Digits(integer = 12, fraction = 2, message = "{validation.calculatedProfit.format}")
    private BigDecimal calculatedProfit;

    private ProfitStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate;

    private PaymentMethod paymentMethod;

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;
}