package com.mahmoud.maalflow.modules.installments.capital.dto;

import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for CapitalTransaction operations.
 * For pooled capital model - partnerId is optional (for audit/tracking only).
 *
 * @author Mahmoud
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapitalTransactionRequest {

    @NotNull(message = "{messages.capital.transactionType.required}")
    private CapitalTransactionType transactionType;

    @NotNull(message = "{messages.capital.amount.required}")
    @Positive(message = "{messages.capital.amount.positive}")
    private BigDecimal amount;

    // Optional: For tracking which partner made the transaction (audit only)
    private Long partnerId;

    private Long contractId;

    @Size(max = 500, message = "{messages.capital.description.size}")
    private String description;
}
