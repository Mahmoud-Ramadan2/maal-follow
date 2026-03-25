package com.mahmoud.maalflow.modules.installments.contract.dto;

import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
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
public class ContractRequest {
    @NotNull(message = "{messages.contract.finalPrice.required}")
    @DecimalMin(value = "100", message = "{messages.contract.finalPrice.invalid}")
    @Digits(integer = 10, fraction = 2, message = "{messages.contract.finalPrice.format}")
    private BigDecimal finalPrice;
    @NotNull(message = "{messages.contract.downPayment.required}")
    @DecimalMin(value = "1", message = "{messages.contract.downPayment.invalid}")
    @Digits(integer = 10, fraction = 2, message = "{messages.contract.downPayment.format}")
    private BigDecimal downPayment;

    @Min(value = 1, message = "{messages.contract.months.min}")
    @Max(value = 60, message = "{messages.contract.months.max}")
    private Integer months;

    @DecimalMin(value = "1.00", message = "{messages.contract.monthlyAmount.invalid}")
    @Digits(integer = 10, fraction = 2, message = "{messages.contract.monthlyAmount.format}")
    private BigDecimal monthlyAmount;

    @NotNull(message = "{messages.contract.startDate.required}")
//    @PastOrPresent(message = "{messages.contract.startDate.invalid}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    private ContractStatus status;
    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;
    @NotNull(message = "{messages.contract.customerId.required}")
    @Positive
    private Long customerId;
    @NotNull(message = "{messages.contract.purchaseId.required}")
    @Positive
    private Long purchaseId;

    // Auto calculated originalPrice = purchase.price + additionalCosts
//    @DecimalMin(value = "1.00", message = "{messages.contract.originalPrice.min}")
//    @Digits(integer = 10, fraction = 2)
//    private BigDecimal originalPrice;

    @DecimalMin(value = "0.00", message = "{messages.contract.additionalCosts.min}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal additionalCosts;
//    @DecimalMin(value = "0.00", message = "{messages.contract.cashDiscountRate.min}")
//    @DecimalMax(value = "100.00", message = "{messages.contract.cashDiscountRate.max}")
//    @Digits(integer = 3, fraction = 2)
//    private BigDecimal cashDiscountRate;

    @DecimalMin(value = "0.00", message = "{messages.contract.earlyPaymentDiscountRate.min}")
    @DecimalMax(value = "100.00", message = "{messages.contract.earlyPaymentDiscountRate.max}")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal earlyPaymentDiscountRate;

    //   Auto calculated profitAmount = finalPrice - originalPrice
//    @DecimalMin(value = "0.00", message = "{messages.contract.profitAmount.min}")
//    @Digits(integer = 10, fraction = 2)
//    private BigDecimal profitAmount;

    @Min(value = 1, message = "{messages.contract.agreedPaymentDay.invalid}")
    @Max(value = 31, message = "{messages.contract.agreedPaymentDay.invalid}")
    private Integer agreedPaymentDay;

    // Partner is optional - for contracts owned by partners
    @Positive(message = "{messages.contract.partnerId.positive}")
    private Long partnerId;

    @Size(max = 50, message = "{messages.contract.contractNumber.size}")
    private String contractNumber;

    private  Long responsibleUserId;

}