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

    @NotNull(message = "{messages.partner.partnerId.required}")
    private Long partnerId;

    @NotNull(message = "{messages.partner.profitDistributionId.required}")
    private Long profitDistributionId;

    @NotNull(message = "{messages.partner.investmentAmount.required}")
    @DecimalMin(value = "0.01", message = "{messages.partner.investmentAmount.min}")
    @Digits(integer = 12, fraction = 2, message = "{messages.partner.investmentAmount.format}")
    private BigDecimal investmentAmount;

    @NotNull(message = "{messages.partner.sharePercentage.required}")
    @DecimalMin(value = "0.00", message = "{messages.partner.sharePercentage.invalid}")
    @DecimalMax(value = "10.00", message = "{messages.partner.sharePercentage.invalid}")
    @Digits(integer = 5, fraction = 2, message = "{messages.partner.sharePercentage.format}")
    private BigDecimal sharePercentage;

    @NotNull(message = "{messages.partner.calculatedProfit.required}")
    @DecimalMin(value = "0.01", message = "{messages.partner.calculatedProfit.min}")
    @Digits(integer = 12, fraction = 2, message = "{messages.partner.calculatedProfit.format}")
    private BigDecimal calculatedProfit;

    private ProfitStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate;

    private PaymentMethod paymentMethod;

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;
}
