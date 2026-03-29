package com.mahmoud.maalflow.modules.installments.schedule.dto;

import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstallmentScheduleRequest {

    @NotNull(message = "{messages.contract.contractId.required}")
    @Positive
    private Long contractId;

    @NotNull(message = "{messages.contract.sequenceNumber.required}")
    @Positive
    private Integer sequenceNumber;

    @NotNull(message = "{messages.contract.profitMonth.required}")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "{messages.contract.profitMonth.format}")
    private String profitMonth;

    @DecimalMin(value = "0.00", message = "{messages.contract.discountApplied.min}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal discountApplied ;

    private Boolean isFinalPayment;

    @NotNull(message = "{messages.contract.amount.required}")
    @DecimalMin(value = "0.01", message = "{messages.contract.amount.min}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    @NotNull(message = "{messages.contract.dueDate.required}")
    private LocalDate dueDate;

    private PaymentStatus status;

    @Size(max = 500, message = "{validation.notes.size}")
    private String notes;

    @Positive(message = "{messages.contract.collectorId.invalid}")
    private Long collectorId;
}